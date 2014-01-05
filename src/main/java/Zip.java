 import java.lang.*;
 import java.util.*;
 import java.util.Date.*;
 import java.util.jar.*;
 import java.util.zip.*;
 import java.io.*;
 import java.text.*;
 import java.text.DateFormat;    
 import java.text.SimpleDateFormat;

 /**
 * Reads GZIP, Zip, and Jar files and outputs their content to the console.
 */
 class Zip {

   final static java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat();

   public static void main(String[] args) 
   {
      
      String fileName = args[0];
      System.out.println("contents of " + fileName + "...");
      if (fileName.endsWith(".gz")) 
      {
        readGZIPFile(fileName);
      } 
      else 
     if (fileName.endsWith(".zip")) 
     {
       System.out.println(getZipFile(fileName));
     } 
     else 
     if (fileName.endsWith(".jar")) 
     {
       System.out.println(getJarFile(fileName));
     }
   } // END OF MAIN


   public static StringBuffer process(String fileName) 
   {  StringBuffer sb = new StringBuffer();
      sb.setLength(0);

      if (fileName.endsWith(".gz")) 
      {
        readGZIPFile(fileName);
        sb.append(fileName);
      } 
      else 
     if (fileName.endsWith(".zip")) 
     {
       sb.append(getZipFile(fileName));
     } 
     else 
     if (fileName.endsWith(".jar")) 
     {
       readJarFile(fileName);
       sb.append(fileName);
     } // end of if

     return sb;
   } // END OF process


    /**
    * Reads a GZIP file and dumps the contents to the console.
    */
   private static void readGZIPFile(String fileName) {
      // use BufferedReader to get one line at a time
      BufferedReader gzipReader = null;
      try 
      {
         // simple loop to dump the contents to the console
         gzipReader = new BufferedReader(
                new InputStreamReader(
                new GZIPInputStream(
                new FileInputStream(fileName))));
         while (gzipReader.ready()) 
         {
            System.out.println(gzipReader.readLine());
         }
         gzipReader.close();
      }  catch (FileNotFoundException fnfe) {
         System.out.println("The file was not found: " + fnfe.getMessage());
      }  catch (IOException ioe) {
         System.out.println("An IOException occurred: " + ioe.getMessage());
      }  finally 
         {
         if (gzipReader != null) 
            {
              try { gzipReader.close(); 
            } catch (IOException ioe) {}
         } // END OF FINALLY
      }
   } // END OF METHOD



   /**
    * Reads a Zip file, iterating through each entry and dumping the contents
    * to the console.
    */
   private static void readZipFile(String fileName) 
   {
      ZipFile zipFile = null;
      try {
         // ZipFile offers an Enumeration of all the files in the Zip file
         zipFile = new ZipFile(fileName);
         for (Enumeration e = zipFile.entries(); e.hasMoreElements();) 
         {
            ZipEntry zipEntry = (ZipEntry) e.nextElement();
            if (!zipEntry.isDirectory()) 
            {
              System.out.println(zipEntry.getName() + " "+zipEntry.getSize() + " " + zipEntry.getTime() + " " + zipEntry.getComment() + " " + zipEntry.getMethod() + ";");
            } // end of if
            // use BufferedReader to get one line at a time
            //BufferedReader zipReader = new BufferedReader(
            //     new InputStreamReader(zipFile.getInputStream(zipEntry)));
            //while (zipReader.ready()) {
            //   System.out.println(zipReader.readLine());
            //}
            //zipReader.close();
         }
      } catch (IOException ioe) {
         System.out.println("An IOException occurred: " + ioe.getMessage());
      } finally {
         if (zipFile != null) {
            try { zipFile.close(); } catch (IOException ioe) {}
         }
      }
   } // END OF METHOD


   /**
    * Reads a Zip file, iterating through each entry putting the contents in a string buffer
    */
   private static StringBuffer getZipFile(String fileName) 
   {
      final String SLASH=new String("\\");
      final String UNIX=new String("/");

      ZipFile zipFile = null;
      StringBuffer sb = new StringBuffer();
      sb.setLength(0);
      String n, t;
      int len=0;

      try {

         // ZipFile offers an Enumeration of all the files in the Zip file
         zipFile = new ZipFile(fileName);

         for (Enumeration e = zipFile.entries(); e.hasMoreElements();) 
         {
            ZipEntry zipEntry = (ZipEntry) e.nextElement();
            if (!zipEntry.isDirectory()) 
            {
              n = zipEntry.getName();
              len = n.lastIndexOf(SLASH)+1;
              if (len<1) len = n.lastIndexOf(UNIX)+1;
              if (len<0) len=0;
System.out.println("len="+len+" and n.length="+n.length());
              t=n.substring(len);
              sb.append(t + " "+zipEntry.getSize() + " " + formatter.format(new Date(zipEntry.getTime())) + ";" + '\r');
            } // end of if
         }
      } catch (IOException ioe) {
         System.out.println("An IOException occurred: " + ioe.getMessage()+ " reading " + fileName);
      } finally {
         if (zipFile != null) {
            try { zipFile.close(); } catch (IOException ioe) {}
         }
      }
      return sb;
   } // END OF METHOD

   /**
    * Reads a Jar file, displaying the attributes in its manifest and dumping
    * the entry contents to a StringBuffer.
    */
   private static StringBuffer getJarFile(String fileName) 
   {
      StringBuffer sb = new StringBuffer();
      sb.setLength(0);
      JarFile jarFile = null;

      try {
         // JarFile extends ZipFile and adds manifest information
         jarFile = new JarFile(fileName);
         if (jarFile.getManifest() != null) 
         {
            //System.out.println("Manifest Main Attributes:");
            Iterator iter = jarFile.getManifest().getMainAttributes().keySet().iterator();
            while (iter.hasNext()) 
            {
               Attributes.Name attribute = (Attributes.Name) iter.next();
               sb.append(attribute + " : " + jarFile.getManifest().getMainAttributes().getValue(attribute)+'\n');
            } // END OF WHILE
            System.out.println();
         } // end of if

         // use the Enumeration to dump the contents of each file to the console
         System.out.println("Jar file entries:");
         for (Enumeration e = jarFile.entries(); e.hasMoreElements();) 
         {
            JarEntry jarEntry = (JarEntry) e.nextElement();
            if (!jarEntry.isDirectory()) 
            {
               sb.append(jarEntry.getName() + " " + jarEntry.getSize() + " " + formatter.format(new Date(jarEntry.getTime())) + '\n');
            } // end of if
         } // end of for
      } 
      catch (IOException ioe) 
          {
             System.out.println("An IOException occurred: " + ioe.getMessage());
          } 
      finally 
          {
             if (jarFile != null) {
                try { jarFile.close(); } 
              catch (IOException ioe) {}
         }
      } // end of try

      return sb;

   } // end of getJarFile method




   /**
    * Reads a Jar file, displaying the attributes in its manifest and dumping
    * the contents of each file contained to the console.
    */
   private static void readJarFile(String fileName) 
   {
      JarFile jarFile = null;

      try {
         // JarFile extends ZipFile and adds manifest information
         jarFile = new JarFile(fileName);
         if (jarFile.getManifest() != null) 
         {
            System.out.println("Manifest Main Attributes:");
            Iterator iter = jarFile.getManifest().getMainAttributes().keySet().iterator();
            while (iter.hasNext()) 
            {
               Attributes.Name attribute = (Attributes.Name) iter.next();
               System.out.println(attribute + " : " +
                   jarFile.getManifest().getMainAttributes().getValue(attribute));
            } // END OF WHILE
            System.out.println();
         } // end of if

         // use the Enumeration to dump the contents of each file to the console
         System.out.println("Jar file entries:");
         for (Enumeration e = jarFile.entries(); e.hasMoreElements();) 
         {
            JarEntry jarEntry = (JarEntry) e.nextElement();
            if (!jarEntry.isDirectory()) 
            {
               System.out.println(jarEntry.getName() + " " + formatter.format(new Date(jarEntry.getTime())));
               //System.out.println(jarEntry.getName() + " " + jarEntry.getSize() + " " + jarEntry.getTime() + " " +
               //                   jarEntry.getComment() + " " + jarEntry.getMethod() + ";");
               //BufferedReader jarReader = new BufferedReader(
               //   new InputStreamReader(jarFile.getInputStream(jarEntry)));
               //while (jarReader.ready()) 
               //{
               //   System.out.println(jarReader.readLine());
               //} // end of while
               //jarReader.close();
            } // end of if
         } // end of for
      } 
      catch (IOException ioe) 
          {
             System.out.println("An IOException occurred: " + ioe.getMessage());
          } 
      finally 
          {
             if (jarFile != null) {
                try { jarFile.close(); } 
              catch (IOException ioe) {}
         }
      }
   } // end of readJarFile
 } // end of class	 

