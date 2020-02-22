// This is the SHELL for the STEP class for the CPU SIM project
// STUDENT NAME: JOe Harkins
// STUDENT STATUS COMMENTS:
// Part1: I believe I've got everything understood for this part. The only thing I didn't really
// understand was how to make a Hex comparison with the OP code so I used the integer representation instead.
// Part2: Thanks for the setup Dr. Leo this part was really straight forward, and I tested it and it appears to decode
// everything properly!
// Part3: I can get almost everything to line up with PEP-8 testing, I can't for the life of me figure out why
// the WO commands have nothing appearing in the Operand sections and it's driving me nuts
//
// Part4: This one was really straight forward, and I believe I have fixed all the errors I had up to this point!
// I changed the WO to true for all STORE functions, fixed my WO from last week and made the code more readable.
// Just fixed the FO = false; in the STORE functions!
//
// Part5: I think I got this one working properly, the only thing not matching up is a carry flag when subtracting
// which we are not handling and the branches which we did not program yet.
//
// Part6: Everything looks good here, a lot of the bitwise operations here were tough to understand, but I got
// a lot of help from Chase McIntyre and I believe this is the desired result. I just had a lot of trouble
// when looking over ASRA and ASRX.
//
// Part7: Everything matches up with the PEP8 code, except the carrybit for sub operations which we defaulted to 0
// so it looks like everything is done!
//----------------------------------------------------------------------------------------------------
import java.awt.*; 
import java.awt.event.*; 
import javax.swing.*; 
//
public class STEP{
//GLOBAL VARIABLES
  
  CPU cpu; // this is the object(instance of a class) that represents the 'STATE' of the CPU
 /* these are the CPU class(Object) private attributes(variables)which represent the 'state' of the CPU
  private int A; private int X; private int PC; private int SP; 
  private byte N; private byte Z; private byte V; private byte C;
  private int IS; private int OS; private int OP;
  private String DESCR; private char MODE; private char [ ] MEMORY;
  //
  // accessors
  public int getPC() // returns PC
  public int getSP() // returns SP
  public int getIS() // returns IS
  public int getOS() // returns OS
  public int getOP() // returns OP
  public int getA()  // returns A
  public int getX()  // returns X
  public byte getN() // returns N
  public byte getZ() // returns Z
  public byte getV() // returns V
  public byte getC() // returns C
  public String getDESCR()  // returns DESCR
  public char getMODE()     // returns MODE
  public char getMEMORY(int address) 
  // IF address is valid it returns MEMORY[address]
  // otherwise it displays an error message and exits
  //
  // mutators
  public void setPC(int pc) // sets PC
  public void setSP(int sp) // sets SP
  public void setIS(int is) // sets IS
  public void setOS(int os) // sets OS
  public void setOP(int op) // sets OP
  public void setA(int a)  // sets A
  public void setX(int x)  // sets X
  public void setN(byte n)  // sets N
  public void setZ(byte n)  // sets Z
  public void setV(byte n)  // sets V
  public void setC(byte n)  // sets C
  public void setDESCR(String descr)  // sets DESCR
  public void setMODE(char mode)      // sets MODE
  public void setMEMORY(int address, char value) 
  // IF address is invalid(out of range) it displays an error message and exits
  // otherwise it sets MEMORY[address] = value
  //
 */
    // GLOBAL VARIABLES
    boolean CO=true; boolean FO=true; boolean WO=false; // logical vars that control the stage execution
 boolean DIS=false; // logical var that controls displaying the updated CPU values to assist debugging
    // 
    boolean Unary=false; // logical var to differentiate between the one/three byte type instructions
    //
    int EA=0;    // this var holds: 1) the address of the instruction to be 'fetched OR 
                 //                 2) the instruction's operand memory effective address; 
                 // this address is used as the INDEX to to read/write from/to memory array
    int NEA=0;   // this a temp var is used for iNdirect address calculating/processing
    char DATA=0; // this var holds the single byte that is read from or written to memory
    int OP=0;    // this var is used to SWITCH on the opcode in the DI() and EX() methods
    //
// the 'main'>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
  public static void main(String args[])
{ new SIMULATOR(); 
    System.out.println("MAIN STARTED_DONOTHING...\n");
} // all the main does is to instantiate an object from the SIMULATOR class
//
// end of the 'main' >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
// this method is invoked by the SIMULATOR when the user clicks the 'STEP' button
// and executes ONE VON NEUMANN CYCLE using/updating the CPU state passed as a parameter
  public void dostep(CPU cpu) { // the VonNeumann loop
           this.cpu = cpu; // the CPU state object
     // reset global vars for the start of a new Instruction cycle
           Unary=false; CO=true; FO=true; WO=false; EA=0; NEA=0; DATA=0; OP=0;
           FI(); // always executed
           DI(); // always executed
    if(CO) CO(); // MAY be skipped
    if(FO) FO(); // MAY be skipped
           EX(); // always executed
    if(WO) WO(); // MAY be skipped
// end of one pass of the VonNeumann cycle
    if(DIS) DISPLAY(); // optional step to display CPU values to the printer
//           
    return;} // end of dostep method
//
//
// START OF Instruction State METHODS
  void FI()
  {
  //
      Unary = false; CO = true; FO = true; WO = false;
      EA = this.cpu.getPC();
      OP = this.cpu.getMEMORY(EA);
      this.cpu.setIS(OP);
      this.cpu.setPC(this.cpu.getPC() + 1);
      EA = this.cpu.getPC();
      if (OP == 0)
      {
          Unary = true;
          String donezo = "STOP";
          this.cpu.setDESCR(donezo);
      }
      else if (OP >= 24 && OP <= 67) // 24-67 is the range for unary operators and 00 from STOP
      {
          Unary = true;
      }
      if (!Unary)
      {
          DATA = this.cpu.getMEMORY(EA);
          this.cpu.setOS(DATA << 8); // I originally just multiplied by 256 to shift the term, but this makes more sense.
          this.cpu.setPC(this.cpu.getPC() + 1);
          EA = this.cpu.getPC();
          DATA = this.cpu.getMEMORY(EA);
          this.cpu.setOS(this.cpu.getOS()+DATA);
          this.cpu.setPC(this.cpu.getPC() + 1);
      }
  }// end of the FI() method
  //
  void DI(){
      // global var OP will hold the Instruction Specifier and will be modified and used to bracket instructions the 'switch'
      // determine if instr. uses the full address mode, ie; the last trinary bracket,
      // if so, then keep the op code and 'r' indicator only by zeroing out the 3 low order bits ('aaa' bits)
      if(cpu.getIS() >= 0x70)
      {
          OP = (cpu.getIS() & 0xF8); //if instr. used the full address mode ('aa'), then keep the op code and reg only
      }
      switch (OP) {
          // in most cases we set: the DESCR string with the instruction's mnemonic,
          // decode address mode and set the MODE, and set the FLAGS to control which other stages are executed( or bypassed)
          case 0x00:
              Unary = true;
              cpu.setDESCR("STOP");
              cpu.setMODE(' ');
              FO = false;
              break;
          case 0x04:
              Unary = false;
              cpu.setDESCR("BR");
              cpu.setMODE('i');
              FO = false;
              break;
          // - Do the rest of the branches -
          case 0x05:
              Unary = false;
              cpu.setDESCR("BR");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x06:
              Unary = false;
              cpu.setDESCR("BRLE");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x07:
              Unary = false;
              cpu.setDESCR("BRLE");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x08:
              Unary = false;
              cpu.setDESCR("BRLT");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x09:
              Unary = false;
              cpu.setDESCR("BRLT");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x0A:
              Unary = false;
              cpu.setDESCR("BREQ");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x0B:
              Unary = false;
              cpu.setDESCR("BREQ");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x0C:
              Unary = false;
              cpu.setDESCR("BRNE");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x0D:
              Unary = false;
              cpu.setDESCR("BRNE");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x0E:
              Unary = false;
              cpu.setDESCR("BRGE");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x0F:
              Unary = false;
              cpu.setDESCR("BRGE");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x10:
              Unary = false;
              cpu.setDESCR("BRGT");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x11:
              Unary = false;
              cpu.setDESCR("BRGT");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x12:
              Unary = false;
              cpu.setDESCR("BRV");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x13:
              Unary = false;
              cpu.setDESCR("BRV");
              cpu.setMODE('x');
              FO = false;
              break;
          case 0x14:
              Unary = false;
              cpu.setDESCR("BRC");
              cpu.setMODE('i');
              FO = false;
              break;
          case 0x15:
              Unary = false;
              cpu.setDESCR("BRC");
              cpu.setMODE('x');
              FO = false;
              break;
          // end of branches bracket
          //
          // start of the unary instr bracket; operates on a REG with no operand
          case 0x18:
              Unary = true;
              cpu.setDESCR("NOTA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          // ................... //
          case 0x19:
              Unary = true;
              cpu.setDESCR("NOTX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1A:
              Unary = true;
              cpu.setDESCR("NEGA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1B:
              Unary = true;
              cpu.setDESCR("NEGX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1C:
              Unary = true;
              cpu.setDESCR("ASLA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1D:
              Unary = true;
              cpu.setDESCR("ASLX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1E:
              Unary = true;
              cpu.setDESCR("ASRA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x1F:
              Unary = true;
              cpu.setDESCR("ASRX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x20:
              Unary = true;
              cpu.setDESCR("ROLA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x21:
              Unary = true;
              cpu.setDESCR("ROLX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x22:
              Unary = true;
              cpu.setDESCR("RORA");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          case 0x23:
              Unary = true;
              cpu.setDESCR("RORX");
              cpu.setMODE(' ');
              CO = false;
              FO = false;
              break;
          // end of unary instr bracket
          //
          // start of trinary instr. bracket; this group specifies both a REG and an addressing MODE
          // since we have made the 3 low order bits ('aaa') zeroes (before entering the SWITCH),
          // we can then identify them by using hex values in increments of 8...
          // for example: 0x70/0x77 are all ADDA with the 8 different modes
          // the addressing mode will be determined after the 'switch'
          case 0x70:
              Unary = false;
              cpu.setDESCR("ADDA");
              break;
          case 0x78:
              Unary = false;
              cpu.setDESCR("ADDX");
              break;
          case 0x80:
              Unary = false;
              cpu.setDESCR("SUBA");
              break;
          case 0x88:
              Unary = false;
              cpu.setDESCR("SUBX");
              break;
          case 0x90:
              Unary = false;
              cpu.setDESCR("ANDA");
              break;
          case 0x98:
              Unary = false;
              cpu.setDESCR("ANDX");
              break;
          case 0xA0:
              Unary = false;
              cpu.setDESCR("ORA");
              break;
          case 0xA8:
              Unary = false;
              cpu.setDESCR("ORX");
              break;
          case 0xB0:
              Unary = false;
              cpu.setDESCR("CPA");
              break;
          case 0xB8:
              Unary = false;
              cpu.setDESCR("CPX");
              break;
          case 0xC0:
              Unary = false;
              cpu.setDESCR("LDA");
              break;
          case 0xC8:
              Unary = false;
              cpu.setDESCR("LDX");
              break;
          case 0xD0:
              Unary = false;
              cpu.setDESCR("LDBYTEA");
              break;
          case 0xD8:
              Unary = false;
              cpu.setDESCR("LDBYTEX");
              break;
          case 0xE0:
              Unary = false;
              cpu.setDESCR("STA");
              WO = true;
              FO = false;
              break;
          case 0xE8:
              Unary = false;
              cpu.setDESCR("STX");
              WO = true;
              FO = false;
              break;
          case 0xF0:
              Unary = false;
              cpu.setDESCR("STBYTEA");
              WO = true;
              FO = false;
              break;
          case 0xF8:
              Unary = false;
              cpu.setDESCR("STBYTEX");
              WO = true;
              FO = false;
              break;
          default:
              Unary = false;
              cpu.setDESCR("INVALID");
              break;
      }
          // end of the 'switch' structure
          //
          // now we need to identify the addressing mode only for the instructions with 8 modes
          if (cpu.getIS() >= 0x70)
          {
              char Addmode = ' ';
              Addmode = (char)(cpu.getIS() & 0x07);
              switch (Addmode)
              {
                  case 0x00:
                      cpu.setMODE('i');
                      break;
                  case 0x01:
                      cpu.setMODE('d');
                      break;
                  case 0x02:
                      cpu.setMODE('n');
                      break;
                  case 0x05:
                      cpu.setMODE('x');
                      break;
                  default:
                      cpu.setMODE(' ');
                      break;
              }
          } //end if

  //
  }// end of the DI() method
  //
  void CO()
  {
      if (CO)/*!Unary*/
      {
          if (cpu.getMODE() == 'x') // INDEXED MODE
          {
              EA = (cpu.getOS() + cpu.getX());
          }
          else if (cpu.getMODE() == 'n') // INDIRECT MODE
          {

              //int byte1 = cpu.getOS(); // index of first byte
              //NEA = (cpu.getMEMORY(byte1) << 8) + (cpu.getMEMORY(byte1+1));
              //System.out.println("NEA is "+ NEA);
              // 2) then the two byte address retrieved from memory IS the effective address (EA)
              //EA = NEA;

              EA = cpu.getOS();
              DATA = cpu.getMEMORY(EA);
              NEA = (DATA << 8);
              EA++; // EA = cpu.getOS() + 1;
              DATA = cpu.getMEMORY(EA);
              NEA += DATA;
              EA = NEA;

              // one liner that might not work
              // EA = (((cpu.getMEMORY(cpu.getOS())) << 8) + (cpu.getMEMORY(cpu.getOS()+1)));

          }
          else if (cpu.getMODE() == 'd') // DIRECT MODE
          {
              EA = cpu.getOS();
          }
          else if (cpu.getMODE() == 'i') // IMMEDIATE MODE
          {
              FO = false;
              cpu.setOP(cpu.getOS());
              EA = cpu.getOS();
          }
          else
          {
              //System.out.println("this should never be accessed... or when STOP is reached?!");
          }
      }
  //
  }; // end of the CO() method
  //
  void FO()
  {
      if (FO)
      {
          if ((cpu.getDESCR().equals("LDBYTEA")) || (cpu.getDESCR().equals("LDBYTEX")))
          {
              //System.out.println("Setting OP for LDBYTEAX");
              //System.out.println("EA is "+EA);
              //System.out.println("mem @ EA is "+(int)cpu.getMEMORY(EA));
              //cpu.OP = cpu.getMEMORY(EA+1);

              // fetch one byte from memory and store it in the cpu OP's low order byte
              //cpu.setOP((cpu.getMEMORY(EA)));
              DATA = cpu.getMEMORY(EA);
              cpu.setOP(DATA);
          }
          else
          {
              //System.out.println("Setting OP to "+((cpu.getMEMORY(EA)<<8)+cpu.getMEMORY(EA+1)));
              //cpu.setOP(((cpu.getMEMORY(EA++)) << 8 ) + cpu.getMEMORY(EA));
              //System.out.println("The EA is currently : "+EA);
              DATA = (char)((cpu.getMEMORY(EA)) << 8);
              cpu.setOP(DATA);
              EA++;
              DATA = (cpu.getMEMORY(EA));
              cpu.setOP(cpu.getOP() + DATA);
          }
      }
  //
  };// end of the FO() method
  //
  void EX(){
  //
      OP = cpu.getIS();
      if (cpu.getIS()>= 0x70) // checks if the instruction used the full address mode
      {
          OP = (cpu.getIS() & 0xF8);
      }

      switch(OP)
      {
          case 0x00:
              System.out.println("STOP INSTRUCTION EXECUTED");
              break;
          // Branch instructions
          case 0x04:
          case 0x05:
              BR();
              break;
          case 0x06:
          case 0x07:
              BRLE();
              break;
          case 0x08:
          case 0x09:
              BRLT();
              break;
          case 0x0A:
          case 0x0B:
              BREQ();
              break;
          case 0x0C:
          case 0x0D:
              BRNE();
              break;
          case 0x0E:
          case 0x0F:
              BRGE();
              break;
          case 0x10:
          case 0x11:
              BRGT();
              break;
          case 0x12:
          case 0x13:
              BRV();
              break;
          case 0x14:
          case 0x15:
              BRC();
              break;
          // End of branch instructions

          // Unary instructions
          case 0x18:
              NOTA();
              break;
          case 0x19:
              NOTX();
              break;
          case 0x1A:
              NEGA();
              break;
          case 0x1B:
              NEGX();
              break;
          case 0x1C:
              ASLA();
              break;
          case 0x1D:
              ASLX();
              break;
          case 0x1E:
              ASRA();
              break;
          case 0x1F:
              ASRX();
              break;
          case 0x20:
              ROLA();
              break;
          case 0x21:
              ROLX();
              break;
          case 0x22:
              RORA();
              break;
          case 0x23:
              RORX();
              break;
          // End of Unary instructions

          // those other instructions!
          case 0x70:
              ADDA();
              break;
          case 0x78:
              ADDX();
              break;
          case 0x80:
              SUBA();
              break;
          case 0x88:
              SUBX();
              break;
          case 0x90:
              ANDA();
              break;
          case 0x98:
              ANDX();
              break;
          case 0xA0:
              ORA();
              break;
          case 0xA8:
              ORX();
              break;
          case 0xB0:
              CPA();
              break;
          case 0xB8:
              CPX();
              break;
          case 0xC0:
              LDA();
              break;
          case 0xC8:
              LDX();
              break;
          case 0xD0:
              LDBYTEA();
              break;
          case 0xD8:
              LDBYTEX();
              break;
          case 0xE0:
              STA();
              break;
          case 0xE8:
              STX();
              break;
          case 0xF0:
              STBYTEA();
              break;
          case 0xF8:
              STBYTEX();
              break;
          // done with the other ones!
          default:
              Unary = false;
              cpu.setDESCR("Dun fucked up");
              break;


      }
  }; // end of the EX() method
  //
  void WO()
  {
  //
      if ((cpu.getDESCR().equals("STBYTEA")) || (cpu.getDESCR().equals("STBYTEX")))
      {
          // I have no idea if this is right because as far as I can tell it never does anything
          //System.out.println("You hit the STBYTEAX block...");
//          System.out.println("EA is "+EA);
//          System.out.println("MEM at EA is "+(cpu.getMEMORY(EA)));
//          System.out.println("OP is "+cpu.getOP());
//          System.out.println("OP as a char is "+(char)(cpu.getOP()));
//          //cpu.setMEMORY(EA,(char)((cpu.getOP())));//<<4)>>8));
          //cpu.setMEMORY(EA,(char)((cpu.getOP())>>8));
          //cpu.setMEMORY(EA+1,(char)(cpu.getOP());
          DATA = (char)((cpu.getOP())& 0x000000FF);// Lo bit
          cpu.setMEMORY(EA,DATA);
      }
      else //STA or STX
      {
          //System.out.println("STA OR STX");
          //cpu.setMEMORY(EA,(char)(cpu.getOP()));
          DATA = (char)((cpu.getOP() >> 8)& 0x000000FF);// Hi bit
          cpu.setMEMORY(EA,DATA);
          EA++;
          DATA = (char)((cpu.getOP()) & 0x000000FF);//
          cpu.setMEMORY(EA,DATA);

      }
  };// end of the WO()
// 
// start of individual instruction execution methods
// these methods will be invoked/called from the EX() method
//
  // start of branches
  // all branches will use the EA calculated in the CO stage
  // since the cpu.OP would have NOT been updated because
  // the FO stage was NOT executed
  void BR()
  {
        //System.out.println("BR INSTRUCTION EXECUTED");
      cpu.setPC(EA);
  } // end of BR()
    //
  void BRLE()
  {
        //System.out.println("BRLE INSTRUCTION EXECUTED");
      if ((cpu.getN() == 1) || (cpu.getZ() == 1))
      {
          cpu.setPC(EA);
      }
  } // end of BRLE()
//
  void BRLT()
  {
       //System.out.println("BRLT INSTRUCTION EXECUTED");
      if (cpu.getN() == 1)
      {
          cpu.setPC(EA);
      }
  } // end of BRLT()
//
  void BREQ()
  {
        //System.out.println("BREQ INSTRUCTION EXECUTED");
      if (cpu.getZ() == 1)
      {
          cpu.setPC(EA);
      }
  } // end of BREQ()
//
  void BRNE()
  {
        //System.out.println("BRNE INSTRUCTION EXECUTED");
      if (cpu.getZ() == 0)
      {
          cpu.setPC(EA);
      }
  } // end of BRNE()
//
  void BRGE()
  {
        //System.out.println("BRGE INSTRUCTION EXECUTED");
      if ((cpu.getN() == 0)) // not negative
      {
          cpu.setPC(EA);
      }
  } // end of BRGE()
//
  void BRGT()
  {
        //System.out.println("BRGT INSTRUCTION EXECUTED");
      if ((cpu.getZ() == 0) && (cpu.getN() == 0)) //not zero and not negative
      {
          cpu.setPC(EA);
      }
  } // end of BRGT()
//
  void BRV()
  {
        //System.out.println("BRV INSTRUCTION EXECUTED");
      if (cpu.getV() == 1)
      {
          cpu.setPC(EA);
      }
  } // end of BRV()
//
  void BRC()
  {
        //System.out.println("BRC INSTRUCTION EXECUTED");
      if (cpu.getC() == 1)
      {
          cpu.setPC(EA);
      }
  } // end of BRC()
//
// start of unary instr.
  void NOTA()
  {

      //System.out.println("NOTA INSTRUCTION EXECUTED");

      cpu.setA(~cpu.getA() & 0x0000FFFF);
      // need to check NZ as per the instructions subset

      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
  } // end of NOTA()
//
  void NOTX()
  {
      //System.out.println("NOTX INSTRUCTION EXECUTED");

      cpu.setX(~cpu.getX() & 0x0000FFFF);
      // need to check NZ

      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
  } // end of NOTX()
//
  void NEGA()
  {
      // System.out.println("NEGA INSTRUCTION EXECUTED");
      // Chase said to check the twos complement before and after are the same

      cpu.setA(-cpu.getA() & 0x0000FFFF);

      // need to set NZV

      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getA() == 0x00008000) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
  }
    // end of NEGA()
//
  void NEGX()
  {
      //System.out.println("NEGX INSTRUCTION EXECUTED");

      cpu.setX(-cpu.getX() & 0x0000FFFF);
      // need to set NZV

      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getX() == 0x00008000) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
  } // end of NEGX()
//
  void ASLA()
  {
      //System.out.println("ASLA INSTRUCTION EXECUTED");

      int Abit = (cpu.getA() & 0x00008000) >> 15; //this gets the highest bit
      cpu.setA(cpu.getA() << 1);
      //System.out.println("The carry bit is "+Abit);
      cpu.setC((byte)Abit);
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (Abit != ((cpu.getA() & 0x00008000)>>15))
      {
          cpu.setV((byte)1);
      }
      else
      {
          cpu.setV((byte)0);
      }


  } // end of ASLA()
//
  void ASLX() {
        //System.out.println("ASLX INSTRUCTION EXECUTED");
      int Xbit = (cpu.getX() & 0x00008000) >> 15; //this gets the highest bit
      // perform the shift on the X register
      // then store the bit
      cpu.setX(cpu.getX() << 1);
      //System.out.println("The carry bit is "+Abit);
      cpu.setC((byte)Xbit);
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (Xbit != ((cpu.getA() & 0x00008000)>>15))
      {
          cpu.setV((byte)1);
      }
      else
      {
          cpu.setV((byte)0);
      }
  } // end of ASLX()
//           
  void ASRA()
  {
        //System.out.println("ASRA INSTRUCTION EXECUTED");
      // need to set NZC
      int Abit = (cpu.getA() & 0x00000001);
      int sign = ((cpu.getA() & 0x00008000));
      cpu.setA(((cpu.getA() >> 1 ) & 0xFFFF7FFF) + sign );
      cpu.setC((byte)Abit);
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      //cpu.setA((cpu.getA() >> 1 ) + Abit);
  } // end of ASRA()
//
  void ASRX() {
        //System.out.println("ASRX INSTRUCTION EXECUTED");
      // need to set NZC
      int Xbit = (cpu.getA() & 0x00000001);
      int sign = ((cpu.getA() & 0x00008000));
      cpu.setX(((cpu.getX() >> 1 ) & 0xFFFF7FFF ) + sign );
      cpu.setC((byte)Xbit);
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
  } // end of ASRX()
//
  void ROLA() {
        //System.out.println("ROLA INSTRUCTION EXECUTED");
      // only need to worry about the C bit
      int leftcarryA = ((cpu.getA() & 0x00008000) >> 15);
      cpu.setA((cpu.getA() << 1) + cpu.getC());
      cpu.setC((byte)leftcarryA);

  } // end of ROLA()
//
  void ROLX() {
        //System.out.println("ROLX INSTRUCTION EXECUTED");
      // only need to worry about the C bit
      int leftcarryX = ((cpu.getX() & 0x00008000) >> 15);
      cpu.setX((cpu.getX() << 1) + cpu.getC());
      cpu.setC((byte)leftcarryX);
  } // end of ROLX()
//
  void RORA() {
       //System.out.println("RORA INSTRUCTION EXECUTED");
      int rightcarryA = ((cpu.getA() & 0x00000001));// >> 1);
      cpu.setA((cpu.getA() >> 1) + (cpu.getC() << 15 ));
      cpu.setC((byte)rightcarryA);
  } // end of RORA()
//
  void RORX() {
        //System.out.println("RORX INSTRUCTION EXECUTED");
      int rightcarryX = ((cpu.getX() & 0x00000001));// >> 1);
      cpu.setA((cpu.getX() >> 1) + (cpu.getC() << 15 ));
      cpu.setC((byte)rightcarryX);
  } // end of RORX()
//
// start of mem/reg instructions
  void ADDA()
  {
      //System.out.println("ADDA INSTRUCTION EXECUTED");
      int A = cpu.getA();
      OP = cpu.getOP();
      cpu.setA(A + OP);
      // we need to set the NZVC status bits here
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if ((((A > 0) && (OP > 0)) && (cpu.getA() < 0)) || (((A < 0) && (OP < 0)) && (cpu.getA() > 0))) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
      A = A + OP;
      if ((A & 0x00010000) == 0x00010000) {
          cpu.setC((byte)1);
      }
      else {
          cpu.setC((byte)0);
      }

  } // end of ADDA()
//
  void ADDX()
  {
      //System.out.println("ADDX INSTRUCTION EXECUTED");
      int X = cpu.getX();
      OP = cpu.getOP();
      cpu.setX(X + OP);
      // we need to set the NZVC status bits here
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if ((((X > 0) && (OP > 0)) && (cpu.getX() < 0)) || (((X < 0) && (OP < 0)) && (cpu.getX() > 0))) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
      X = X + OP;
      if ((X & 0x00010000) == 0x00010000) {
          cpu.setC((byte)1);
      }
      else {
          cpu.setC((byte)0);
      }


  } // end of ADDX()
//
  void SUBA()
  {
      //System.out.println("SUBA INSTRUCTION EXECUTED");
      int A = cpu.getA();
      OP = cpu.getOP();
      cpu.setA(A - OP);
      // we need to set the NZVC status bits here
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      // I improvised with this statement because (-OP > 0) should be the same as (OP < 0)
      if ((((A > 0) && ((OP < 0)) && (cpu.getA() < 0))) || (((A < 0) && (OP > 0)) && (cpu.getA() > 0))) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
      cpu.setC((byte)0); // because this is too difficult to do what we were doing before.
  } // end of SUBA()
//
  void SUBX()
  {
      //System.out.println("SUBX INSTRUCTION EXECUTED");
      int X = cpu.getX();
      OP = cpu.getOP();
      cpu.setX(X - OP);
      // we need to set the NZVC status bits here
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      // I improvised with this statement because (-OP > 0) should be the same as (OP < 0)
      if ((((X > 0) && ((OP < 0)) && (cpu.getX() < 0))) || (((X < 0) && (OP > 0)) && (cpu.getX() > 0))) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
      cpu.setC((byte)0); // because this is too difficult to do what we were doing before.

  } // end of SUBX()
//
  void ANDA()
  {
      //System.out.println("ANDA INSTRUCTION EXECUTED");
      OP = cpu.getOP();
      cpu.setA(cpu.getA() & OP);
      // we need to set the NZ status bits/flags here
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of ANDA()
//
  void ANDX()
  {
      //System.out.println("ANDX INSTRUCTION EXECUTED");
      OP = cpu.getOP();
      cpu.setX(cpu.getX() & OP);
      // we need to set the NZ status bits/flags here
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of ANDX()
//
  void ORA()
  {
      //System.out.println("ORA INSTRUCTION EXECUTED");
      OP = cpu.getOP();
      cpu.setA(cpu.getA() | OP);
      // we need to set the NZ status bits/flags here
      if ((cpu.getA() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of ORA()
//
  void ORX()
  {
      //System.out.println("ORX INSTRUCTION EXECUTED");
      OP = cpu.getOP();
      cpu.setX(cpu.getX() | OP);
      // we need to set the NZ status bits/flags here
      if ((cpu.getX() & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of ORX()
//
  void CPA()
  {
      //System.out.println("CPA INSTRUCTION EXECUTED");
      int A = cpu.getA();
      OP = cpu.getOP();
      int temp = A - OP;

      // we need to set the NZVC status bits here because thats all that CP(A|X) does

      if (temp == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((temp & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      // I improvised with this statement because (-OP > 0) should be the same as (OP < 0)
      if ((((A > 0) && ((OP < 0)) && (temp < 0))) || (((A < 0) && (OP > 0)) && (temp > 0))) {
          cpu.setV((byte)1);
      }
      else {
          cpu.setV((byte)0);
      }
      cpu.setC((byte)0); // because this is too difficult to do what we were doing before.

  } // end of CPA()
//
  void CPX()
  {
      //System.out.println("CPX INSTRUCTION EXECUTED");
      int X = cpu.getX();
      OP = cpu.getOP();
      int temp = X - OP;

      // we need to set the NZVC status bits here because thats all that CP(A|X) does

      if (temp == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
      if ((temp & 0x00008000) == 0x00008000){
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      // I improvised with this statement because (-OP > 0) should be the same as (OP < 0)
      if ((((X > 0) && ((OP < 0)) && (temp < 0))) || (((X < 0) && (OP > 0)) && (temp > 0)))
      {
          cpu.setV((byte)1);
      }
      else
      {
          cpu.setV((byte)0);
      }
      cpu.setC((byte)0); // because this is too difficult to do what we were doing before.

  } // end of CPX()
//
  void LDA()
  {
      //System.out.println("LDA INSTRUCTION EXECUTED");
      OP = cpu.getOP(); // we will set the REGISTER to this value then check for NZ bit flags
      cpu.setA(OP);
      if ((cpu.getA()&0x00008000) == 0x00008000) {
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of LDA()
//
  void LDX()
  {
      //System.out.println("LDX INSTRUCTION EXECUTED");
      OP = cpu.getOP(); // we will set the REGISTER to this value then check for NZ bit flags
      cpu.setX(OP);
      if ((cpu.getX()&0x00008000) == 0x00008000) {
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }

  } // end of LDX()
//
  void LDBYTEA()
  {
        //System.out.println("LDBYTEA INSTRUCTION EXECUTED");
      OP = cpu.getOP(); // this is the one byte already made ready to go into REGISTER A
      cpu.setA((cpu.getA() & 0xFFFFFF00) + OP);

      // now checking the NZ flags.
      if ((cpu.getA()&0x00008000) == 0x00008000) {
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getA() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
  } // end of LDBYTEA()
//
  void LDBYTEX()
  {
      //System.out.println("LDBYTEX INSTRUCTION EXECUTED");
      OP = cpu.getOP(); // this is the one byte already made ready to go into REGISTER A
      cpu.setX((cpu.getX() & 0xFFFFFF00) + OP);

      // now checking the NZ flags.
      if ((cpu.getX()&0x00008000) == 0x00008000) {
          cpu.setN((byte)1);
      }
      else {
          cpu.setN((byte)0);
      }
      if (cpu.getX() == 0) {
          cpu.setZ((byte)1);
      }
      else {
          cpu.setZ((byte)0);
      }
  } // end of LDBYTEX()
//
  void STA()
  {
      cpu.setOP(cpu.getA());
        //System.out.println("STA INSTRUCTION EXECUTED");
  } // end of STA()
//
  void STX()
  {
      cpu.setOP(cpu.getX());
      //System.out.println("STX INSTRUCTION EXECUTED");
  } // end of STX()
//
  void STBYTEA()
  {
      cpu.setOP(cpu.getA() & 0x000000FF);
      //cpu.setA((cpu.getA() & 0xFFFFFF00) + OP); // we erase the single byte at the end and add in the OP byte?

      //System.out.println("STBYTEA INSTRUCTION EXECUTED");
  } // end of STBYTEA()
//
  void STBYTEX()
  {
      //System.out.println("STBYTEX INSTRUCTION EXECUTED");
      cpu.setOP(cpu.getX() & 0x000000FF);
  } // end of STBYTEX()
//
  void DISPLAY() { // optional step to display CPU values to the printer
  System.out.printf("\nPC: 0x%04x, SP: 0x%04x, IS: 0x%02x, OS: 0x%04x, OP: 0x%04x, A: 0x%04x, X: 0x%04x\n",
                      cpu.getPC(), cpu.getSP(), cpu.getIS(), cpu.getOS(), cpu.getOP(), cpu.getA(), cpu.getX());
  System.out.printf("N: 0x%01x, Z: 0x%01x, V: 0x%01x, C: 0x%01x, DESCR: %s, MODE: %c\n",
                      cpu.getN(), cpu.getZ(), cpu.getV(), cpu.getC(), cpu.getDESCR(), cpu.getMODE());
  } // end of DISPLAY()
//
static class CPU {  
  private int A; private int X; private int PC; private int SP;  
  private byte N; private byte Z; private byte V; private byte C; 
  private int IS; private int OS; private int OP; 
  private String DESCR; private char MODE; private char [ ] MEMORY; 
  // 
  public CPU(int a, int x, int pc, int sp, byte n, byte z, byte v, byte c, int is, 
             int os, int op, String descr, char mode, char [ ] memory){ 
  A=a; X=x; PC=pc; SP=sp; N=n; Z=z; V=v; C=c; IS=is; OS=os; OP=op; 
  DESCR=descr; MODE=mode; MEMORY=memory;    
  } // end CPU constructor 
  // accessors 
  public int getPC() { return PC; } 
  public int getSP() { return SP; } 
  public int getIS() { return IS; } 
  public int getOS() { return OS; } 
  public int getOP() { return OP; } 
  public int getA()  { return A; } 
  public int getX()  { return X; } 
  public byte getN() { return N; } 
  public byte getZ() { return Z; } 
  public byte getV() { return V; } 
  public byte getC() { return C; } 
  public String getDESCR()  { return DESCR; } 
  public char getMODE()     { return MODE; } 
  public char getMEMORY(int address)  
  {  if( address < 0 || address > MEMORY.length)  
     {System.out.printf("\ngetMEMORY() called with invalid memory address:0x%04x\n",address);System.exit(1);return(0);} 
     else return (MEMORY[address]); }  
  public void setPC(int pc) {PC=pc;   } 
  public void setSP(int sp) {SP=sp;   } 
  public void setIS(int is) {IS=is;   } 
  public void setOS(int os) {OS=os;   } 
  public void setOP(int op) {OP=op;   } 
  public void setA(int a)   {A=a;   } 
  public void setX(int x)   {X=x;   } 
  public void setN(byte n)  {N=n;   } 
  public void setZ(byte z)  {Z=z;   } 
  public void setV(byte v)  {V=v;   } 
  public void setC(byte c)  {C=c;   } 
  public void setDESCR(String descr)  {DESCR=descr;   } 
  public void setMODE(char mode)      {MODE=mode;   } 
  public void setMEMORY(int address, char value)  
  {if( address < 0 || address > MEMORY.length)  
   {System.out.printf("\nsetMEMORY() called with invalid memory address:0x%04x\n",address);System.exit(1);} 
   else MEMORY[address] = value; }  
  // 
} // end class CPU  
//
//
// the class itself  
static class SIMULATOR extends Frame implements ActionListener 
{ 
Button start,step,end; 
TextField N; 
TextField Z; 
TextField V; 
TextField C; 
TextField A1; 
TextField A2; 
TextField X1; 
TextField X2; 
TextField SP1; 
TextField SP2; 
TextField PC1; 
TextField PC2; 
TextField IS1; 
TextField IS2; 
TextField OS1; 
TextField OS2; 
TextField OP1; 
TextField OP2; 
// 
Label space; 
Label lblN; 
Label lblZ; 
Label lblV; 
Label lblC; 
Label lblA; 
Label lblX; 
Label lblSP; 
Label lblPC; 
Label lblIS; 
Label lblOS; 
Label lblOP; 
// 
Panel pan1,pan2,pan3; 
double no1,no2; 
double ans; 
char oper; 
// 
CPU cpu; 
STEP Step; 
// constructor 
public SIMULATOR() 
{  
// 
pan1= new Panel(); 
pan2= new Panel(); 
pan3= new Panel(); 
// 
end=new Button("end"); 
start=new Button("start"); 
step=new Button("step"); 
// 
space= new Label(" "); 
lblN = new Label(" N"); 
lblZ = new Label(" Z"); 
lblV = new Label(" V"); 
lblC = new Label(" C"); 
lblA = new Label("            Accumulator"); 
lblX = new Label("         Index register"); 
lblSP= new Label("          Stack pointer"); 
lblPC= new Label("        Program Counter"); 
lblIS= new Label(" Instruction Specifier"); 
lblOS= new Label("     Operand Specifier"); 
lblOP= new Label("             (Operand)"); 
// 
N=new TextField("0",1); 
Z=new TextField("0",1); 
V=new TextField("0",1); 
C=new TextField("0",1); 
A1=new TextField("0",6); 
A2=new TextField("0",6); 
X1=new TextField("0",6); 
X2=new TextField("0",6); 
SP1=new TextField("0",6); 
SP2=new TextField("0",6); 
PC1=new TextField("0",6); 
PC2=new TextField("0",6); 
IS1=new TextField("0",16); 
IS2=new TextField("0",10); 
OS1=new TextField("0",6); 
OS2=new TextField("0",6); 
OP1=new TextField("0",6); 
OP2=new TextField("0",6); 
// 
pan3.add(start); 
pan3.add(step); 
pan3.add(end); 
// 
setLayout(new BorderLayout()); 
pan2.setLayout( new GridLayout( 8, 2 ) ); 
pan1.add(lblN); 
pan1.add(N); 
pan1.add(lblZ); 
pan1.add(Z); 
pan1.add(lblV); 
pan1.add(V); 
pan1.add(lblC); 
pan1.add(C); 
// 
pan2.add(lblA); 
pan2.add(A1); 
pan2.add(A2); 
pan2.add(lblX); 
pan2.add(X1); 
pan2.add(X2); 
pan2.add(lblSP); 
pan2.add(SP1); 
pan2.add(SP2); 
pan2.add(lblPC); 
pan2.add(PC1); 
pan2.add(PC2); 
pan2.add(lblIS); 
pan2.add(IS1); 
pan2.add(IS2); 
pan2.add(lblOS); 
pan2.add(OS1); 
pan2.add(OS2); 
pan2.add(lblOP); 
pan2.add(OP1); 
pan2.add(OP2); 
// 
add(pan1,"North"); 
add(pan2,"Center"); 
add(pan3,"South"); 
// 
space.setEnabled(false); 
lblN.setEnabled(false); 
lblZ.setEnabled(false); 
lblV.setEnabled(false); 
lblC.setEnabled(false); 
lblA.setEnabled(false); 
lblX.setEnabled(false); 
lblSP.setEnabled(false); 
lblPC.setEnabled(false); 
lblIS.setEnabled(false); 
lblOS.setEnabled(false); 
lblOP.setEnabled(false); 
N.setEnabled(false); 
Z.setEnabled(false); 
V.setEnabled(false); 
C.setEnabled(false); 
A1.setEnabled(false); 
A2.setEnabled(false); 
X1.setEnabled(false); 
X2.setEnabled(false); 
SP1.setEnabled(false); 
SP2.setEnabled(false); 
PC1.setEnabled(false); 
PC2.setEnabled(false); 
IS1.setEnabled(false); 
IS2.setEnabled(false); 
OS1.setEnabled(false); 
OS2.setEnabled(false); 
OP1.setEnabled(false); 
OP2.setEnabled(false); 
// 
end.addActionListener(this); 
start.addActionListener(this); 
step.addActionListener(this); 
// 
setTitle("PEP8-SIMULATOR"); 
setSize(600,600); 
setVisible(true); 
// 
  Step = new STEP(); 
  int a = Integer.parseInt(A2.getText()); 
  int x = Integer.parseInt(X2.getText()); 
  int pc = Integer.parseInt(PC2.getText()); 
  int sp = Integer.parseInt(SP2.getText()); 
  byte n = Byte.parseByte(N.getText()); 
  byte z = Byte.parseByte(C.getText()); 
  byte v = Byte.parseByte(V.getText()); 
  byte c = Byte.parseByte(C.getText()); 
  int is = Integer.parseInt(IS1.getText()); 
  int os = Integer.parseInt(OS2.getText()); 
  int op = Integer.parseInt(OP2.getText()); 
  String descr= "Start"; 
  char mode='d'; 
  char [ ] memory = {// simulates main memory(the instructions to be processed and data to be used 
0xC1,0x00,0x49,0x22,0x20,0x1C,0x1E,0x1A,0x18,0xA0,0xFF,0xFF,0x0E,0x00,0x09,0x90, 
0x00,0x00,0x10,0x00,0x0F,0x70,0x00,0x01,0x81,0x00,0x4B,0xF1,0x00,0x4D,0x91,0x00, 
0x4F,0xD1,0x00,0x4D,0xC9,0x00,0x4F,0x72,0x00,0x51,0x85,0x00,0x53,0xE1,0x00,0x4D, 
0x78,0x00,0x01,0xE9,0x00,0x4F,0xC5,0x00,0x51,0xB8,0x00,0x02,0x08,0x00,0x21,0x06, 
0x00,0x21,0x0A,0x00,0x21,0x04,0x00,0x48,0x00,0xBF,0xFF,0x00,0x03,0x00,0x00,0x00, 
0x00,0x00,0x53,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x00  
  /* 
    0x04,0x00,0x18,0xBF,0xFF,0x00,0x03,0x00,0x00,0x00,0x00,0x00,0x0D,0x00,0x01,0x02, 
    0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x00,0xC1,0x00,0x03,0x22,0x20,0x1C,0x1E,0x1A, 
    0x18,0xA0,0xFF,0xFF,0x90,0x00,0x00,0x70,0x00,0x01,0x81,0x00,0x05,0xF1,0x00,0x07, 
    0xD1,0x00,0x07,0xC9,0x00,0x09,0x72,0x00,0x0B,0x85,0x00,0x0D,0xE1,0x00,0x07,0x78, 
    0x00,0x01,0xE9,0x00,0x09,0xC5,0x00,0x0B,0xB8,0x00,0x02,0x08,0x00,0x30,0x06,0x00, 
    0x30,0x0A,0x00,0x30,0x00,0x00 
 */ 
 };  
  cpu= new CPU( a, x, pc, sp, n, z, v, c, is, os, op, descr, mode, memory); 
// 
} // end constructor 
// this is the method that reacts to the push of a button, in our case the 'STEP' button 
public void actionPerformed(ActionEvent e) 
{ 
Button btn= (Button)e.getSource();String fs; 
if(btn==end) {System.exit(0);} // end if 
// 
if(btn==step) 
{  
  Step.dostep(cpu); 
  // 
  N.setText(Byte.toString(cpu.getN())); 
  Z.setText(Byte.toString(cpu.getZ())); 
  V.setText(Byte.toString(cpu.getV())); 
  C.setText(Byte.toString(cpu.getC())); 
  fs = String.format("0x%04X",(short)cpu.getA()); 
  A1.setText(fs); 
  A2.setText(Short.toString((short)cpu.getA())); 
  fs = String.format("0x%04X",(short)cpu.getX()); 
  X1.setText(fs); 
  X2.setText(Short.toString((short)cpu.getX())); 
  fs = String.format("0x%04X",(short)cpu.getPC()); 
  PC1.setText(fs); 
  PC2.setText(Short.toString((short)cpu.getPC())); 
  fs = String.format("0x%02X - ",cpu.getIS()); 
  fs= fs + toBinaryStringOfLength(cpu.getIS(),8); 
  IS1.setText(fs); 
  fs = String.format("0x%04X",(short)cpu.getOS()); 
  OS1.setText(fs); 
  OS2.setText(Short.toString((short)cpu.getOS())); 
    if(cpu.getDESCR().equals("STBYTEA") || cpu.getDESCR().equals("STBYTEX") || 
       cpu.getDESCR().equals("LDBYTEA") || cpu.getDESCR().equals("LDBYTEX") ){ 
        fs = String.format("0x%02X",(short)cpu.getOP());} 
  else{ fs = String.format("0x%04X",(short)cpu.getOP());} 
  OP1.setText(fs); 
  OP2.setText(Short.toString((short)cpu.getOP())); 
   
  if (cpu.getMODE() != ' '){IS2.setText(cpu.getDESCR() + ", " + cpu.getMODE());} 
  else {IS2.setText(cpu.getDESCR()); OS1.setText(" ");OS2.setText(" ");OP1.setText(" ");OP2.setText(" ");} 
  if(cpu.getIS() == 0x00) {step.setEnabled(false); } // disable the step button 
  if(cpu.getDESCR().equals("STOP")) {step.setEnabled(false); } // disable the step button 
//display(cpu); 
} 
// 
if(btn==start) 
{ 
space.setEnabled(true); 
lblN.setEnabled(true); 
lblZ.setEnabled(true); 
lblV.setEnabled(true); 
lblC.setEnabled(true); 
lblA.setEnabled(true); 
lblX.setEnabled(true); 
lblSP.setEnabled(true); 
lblPC.setEnabled(true); 
lblIS.setEnabled(true); 
lblOS.setEnabled(true); 
lblOP.setEnabled(true); 
N.setEnabled(true); 
Z.setEnabled(true); 
V.setEnabled(true); 
C.setEnabled(true); 
A1.setEnabled(true); 
A2.setEnabled(true); 
X1.setEnabled(true); 
X2.setEnabled(true); 
SP1.setEnabled(true); 
SP2.setEnabled(true); 
PC1.setEnabled(true); 
PC2.setEnabled(true); 
IS1.setEnabled(true); 
IS2.setEnabled(true); 
OS1.setEnabled(true); 
OS2.setEnabled(true); 
OP1.setEnabled(true); 
OP2.setEnabled(true); 
} // end if  
 
} // end action performed 
// 
// void display(cpu) { } 
// 
public String toBinaryStringOfLength(int value, int length) { 
    String binaryString = Integer.toBinaryString(value);  
    StringBuilder leadingZeroes = new StringBuilder(); 
    for(int index = 0; index < length - binaryString.length(); index++) { 
        leadingZeroes = leadingZeroes.append("0"); 
    } 
    return leadingZeroes + binaryString; 
} 
//  
} // end SIMULATOR class 
//
//
} // class step
