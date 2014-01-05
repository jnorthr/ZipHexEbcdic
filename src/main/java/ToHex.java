
import javax.swing.*;
import java.io.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * Class to output bytes of text from an input file with their hex equivalent
 * values. Each byte has it's hex equivalent shown under it on two lines. Since java integral types
 * are all signed, this implementation is not robust and will produce ? char.s if byte translation
 * is suspect. For example -128 etc values need more work.
 */
public class ToHex
        extends JFrame
        implements KeyListener

{
    private final boolean TOP = true;
    private final boolean BOTTOM = false;
    private String fileName=null;
    private DataInputStream input;
    private char[] hex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','?'};
    private StringBuffer line1 = new StringBuffer(110);
    private StringBuffer line2 = new StringBuffer(110);
    private StringBuffer line3 = new StringBuffer(110);
    private long numberOfBytes=0;
    private long countOfBytes=0;
    final static char cr1 = (char)169; // use the copyright char.for 0xOD
    final static char cr2 = (char)174; // use the Registered symbol for 0xOA

    private JTextArea jta;
    private JScrollPane jsp;

    /**
     * A default class constructor to obtain the filename from JFileChooser then get filesize in bytes.
     * @throws IOException  If filename is a directory, cannot be read, or does not exist.
     */
	public ToHex() throws IOException
	{
        super();
        browse();
        File f = new File(fileName);
        if (f.exists()&f.isFile()&f.canRead())
        {
            numberOfBytes=f.length();
            setTitle(fileName+" "+numberOfBytes+" bytes (F12 to close)");
        } // end of if
        else
        {
            throw new FileNotFoundException(fileName+" cannot be found or used");
        }
	} // end of ToHex constructor

    /**
     * A non-default class constructor to obtain the filesize in bytes.
     * @param filename  A string identifier of the actual file to be used as input
     * @throws IOException  If filename is a directory, cannot be read, or does not exist.
     */
	public ToHex(String filename) throws IOException
	{
        super();
        fileName = filename;
        File f = new File(filename);
        if (f.exists()&f.isFile()&f.canRead())
        {
            numberOfBytes=f.length();
            setTitle(filename+" "+numberOfBytes+" bytes (F12 to close)");
        } // end of if
        else
        {
            throw new FileNotFoundException(filename+" cannot be found or used");
        }
	} // end of ToHex constructor

    /**
     * Main method to permit stand alone execution
     * @param args      Parm#1 is valid filename to view, if no arg then use JFileChooser to pick it
     * @throws IOException  when file cannot be found,or read, or opened,or is a directory
     */
    public static void createGUI() // ?? doesn't like this -> throws IOException

    {   // set a static flag to remember we are running from a command line

        { // leave it to internal JFileChooser to pick file to view
            try
            {
                ToHex th = new ToHex();
                th.convert();
            } // end of try
            catch (IOException e)
            {
                String msg = "Your choice";
                String text = " cannot be viewed for some reason";
                javax.swing.JOptionPane.showMessageDialog(null, text, msg,
                           javax.swing.JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } // end of catch
        } // end of else

    } // end of method()

    /**
     * Principle method to show the contents of the file in hexadecimal format. Uses either F3
     * or F12 function keys to end session. These are std IBM 3270 key strokes. F3 to exit and
     * F12 to cancel (or backstep to previous task)
     */
	protected void convert()
	{
        setSize(800,600);
        jta = new JTextArea();
        jta.setFont(new Font("Monospaced",Font.PLAIN, 12));
        jta.setEditable(true);
        jsp = new JScrollPane(jta);
        jta.addKeyListener(this);


        getContentPane().add(jsp,BorderLayout.CENTER);

		try
		{
            FileInputStream input = new FileInputStream(fileName);

            byte c;
            char d;
	    	int upperC, lowerC=0;
			long chcnt = 0; // counts number of char.s this line set
            clear(line1);
            clear(line2);
            clear(line3);

            header(TOP); // create column number header

            // principle input loop
	    	while(input.available() != 0)
	    	{
	    		c = (byte)input.read();
                countOfBytes+=1;
                if (countOfBytes>numberOfBytes)
                    break;
                d = (char)c;
                // chose to use arithmetic to derive a pair of Hex codes for each byte rather than
                // using Integer.toHexString() as that works for positive numbers but not negative
                // numbers; Use of >>> possible but needs more thought
                lowerC = c%16;
                upperC = c / 16;
                chcnt += 1; // count of char.s this line

                // Insert character equivalent
                switch (c)
                {   // print cr, lf char.s as copyright and registered symbols or
                    // print char. equivalent if possible else print a blank
                    case 13: line1.append(cr1);
                             break;
                    case 10: line1.append(cr2);
                             break;
                    default: line1.append( (c>29 & c<150) ? d : ' ');
                } // end of switch

                // special handling of bytes where 'sign' bit is set like '1010 0001'=0xA1
                if (c<0){
                    int k = 0 - c;
                    int l = k>>4;
                    int m = l+8;
                    if (m>0&m<16)
                    {
                        upperC=m;
                    }
                    else
                        upperC=16; // unknown combination:set to hex(16)=?
                } // end of if

                // sort out correct value for lower digit of a byte
                if (lowerC<0)
                {
                   lowerC = 0 - lowerC; // reverse the sign
                } // end of if

                if (lowerC < 0 | lowerC > 16)
                      lowerC=16; // unknown combination:set to hex(16)=?

                // show failure point if byte has not beed decoded correctly
                if (upperC < 0 || upperC > 16 || lowerC <0 || lowerC > 16)
                {
                    System.out.println("c="+c+" @"+countOfBytes+" upperC="+upperC+" lowerC="+lowerC);
                    printAll();
                    System.exit(1);
                }

                // compose hex digits to output buffer
                line2.append(hex[upperC]);
                line3.append(hex[lowerC]);
                if (chcnt>99)
                { // allow 100 char.s per line then dump it
                    printAll();
                    chcnt=0;
                } // end of if
	  		} // end of while

            // pad output lines with trailing blanks on EOF
            while (chcnt++<100)
            {
                line1.append(' ');
                line2.append(' ');
                line3.append(' ');
            } // end of while

            printAll();
            header(BOTTOM); // create column number header

			//output.print(s);
			input.close();

            jsp.enableInputMethods(false);
            jta.setCaretPosition(1);
            setVisible(true);

//			output.close();
	  	} // end of try

	  	catch(Exception e)
	  	{
	  		e.printStackTrace();
	  	}
	} // end of convert

    /**
     * Convenience method to reset stringbuffer
     * @param s     StringBuffer to be reset
     */
    void clear(StringBuffer s)
    {
        s.delete(0,s.length());
        s.append(' ');
    } // end of clear()

    /**
     * Convenience method to output a line of text
     */
    void print(StringBuffer s)
    {
        jta.append(s+ "\n");
        clear(s);
    } // end of print()

    /**
     * Convenience method to output each 100 bytes of input file
     * as several lines of hex text.
     */
    void printAll()
    {
        line1.append(' ');
        line1.append(countOfBytes);
        print(line1);
        print(line2);
        print(line3);
        jta.append("\n");
    } // end of printAll()

    /**                                               0    1
     * Convenience method to insert column header ....5....0 etc
     */
    void header(boolean top)
    {
        char period = (top) ? '.' : ' '; // if top of form, set period value for line2.append
        for (int i=1;i<101;i++)
        {
            if (!top)
            {
                line3.append('.');
            }
            String s = ""+(byte)(i/10);

            if(i%10==0)
            {
                line1.append(s.charAt(0));
                line2.append('0');
            }
            else
            {
                line1.append( (i%5==0) ? s.charAt(0) : ' ');
                line2.append( (i%5==0) ? '5' : period );
            } // end of else
        } // end of for

        // dump current contents
        if (!top)
        {
            print(line3);
        } // end of if
        print(line1);
        print(line2);

    } // end of header()

    /**
     * A do nothing implementation.
     * @param ke
     */
    public void keyPressed(KeyEvent ke)
    {
    } // end of keyPressed method

    /**
     * This method traps F12 function key usage to signal end of task
     *
     * @param ke        standard KeyEvent object
     */
     public void keyReleased(KeyEvent ke)
     {
        // end task via F12 key - this is the standard IBM 'cancel' key on mainframes
        if (ke.getKeyCode()==KeyEvent.VK_F12 | ke.getKeyCode()==KeyEvent.VK_F3)
        {
            //this.hide();
            this.dispose();
            System.exit(0);
        } // end of if

        if (ke.getKeyCode()==KeyEvent.VK_UP)
        {
        } // end of VK_UP

        if (ke.getKeyCode()==KeyEvent.VK_DOWN)
        {
        } // end of VK_DOWN

        if (ke.getKeyCode()==KeyEvent.VK_LEFT)
        {
        } // end of VK_LEFT

        if (ke.getKeyCode()==KeyEvent.VK_RIGHT)
        {
        } // end of VK_RIGHT

    } // end of keyReleased method

    /**
     * A do nothing implementation
     * @param ke
     */
    public void keyTyped(KeyEvent ke)
    { // empty method
    } // end of keyTyped method


    /**
     * Choose a file to see in hex by browsing to the file of your choice or
     * typing a file name.
     */
	protected void browse()
            throws IOException
	{
		String initialDirectory = System.getProperty("user.dir");
		javax.swing.JFileChooser fileDialog = new javax.swing.JFileChooser(initialDirectory);

		if (fileDialog.showDialog(null,"Select") == javax.swing.JFileChooser.APPROVE_OPTION)
		{
			try
			{
				File f = fileDialog.getSelectedFile();
				fileName = f.getAbsolutePath();
			}
			catch(Exception e)
			{
                        throw new FileNotFoundException(fileName+" cannot be found or used");
			}
		} // end of APPROVE_OPTION

	} // END OF browse()


    public static void main(String[] args) 
    {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()  
        {
            public void run() 
            {
                createGUI();
            }
        });
    } // end of main

} // end of ToHex class
