/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008-2009 Sun Microsystems, Inc.
 */
package org.opends.server.util;



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * This class provides an interface for generating self-signed certificates and
 * certificate signing requests, and for importing, exporting, and deleting
 * certificates from a key store.  It supports JKS, JCEKS PKCS11, and PKCS12 key
 * store types.
 * <BR><BR>
 * Note that for some operations, particularly those that require updating the
 * contents of a key store (including generating certificates and/or certificate
 * signing  requests, importing certificates, or removing certificates), this
 * class relies on the keytool utility provided with Sun's implementation of the
 * Java runtime  environment.  It will perform the associated operations by
 * invoking the appropriate command.  It is possible that the keytool command
 * will not exist in all Java runtime environments, especially those not created
 * by Sun.  In those cases, it will not be possible to invoke operations that
 * require altering the contents of the key store.  Therefore, it is strongly
 * recommended that any code that may want to make use of this facility should
 * first call {@code mayUseCertificateManager} and if it returns {@code false}
 * the caller should gracefully degrade and suggest that the user perform the
 * operation manually.
 *
 * This version of the class fixes this issue:
 *
 * https://opends.dev.java.net/issues/show_bug.cgi?id=3752
 */
@org.opends.server.types.PublicAPI(
     stability=org.opends.server.types.StabilityLevel.VOLATILE,
     mayInstantiate=true,
     mayExtend=false,
     mayInvoke=true)
public final class CertificateManager
{
  /**
   * The path to the keytool command, which will be required to perform
   * operations that modify the contents of a key store.
   */
  public static final String KEYTOOL_COMMAND;



  /**
   * The key store type value that should be used for the "JKS" key store.
   */
  public static final String KEY_STORE_TYPE_JKS = "JKS";

  /**
   * The key store type value that should be used for the "JCEKS" key store.
   */
  public static final String KEY_STORE_TYPE_JCEKS = "JCEKS";

  /**
   * The key store type value that should be used for the "PKCS11" key store.
   */
  public static final String KEY_STORE_TYPE_PKCS11 = "PKCS11";



  /**
   * The key store type value that should be used for the "PKCS12" key store.
   */
  public static final String KEY_STORE_TYPE_PKCS12 = "PKCS12";



  /**
   * The key store path value that must be used in conjunction with the PKCS11
   * key store type.
   */
  public static final String KEY_STORE_PATH_PKCS11 = "NONE";



  // The parsed key store backing this certificate manager.
  private KeyStore keyStore;

  // The password that should be used to interact with the key store.
  private String keyStorePIN;

  // The path to the key store that we should be using.
  private String keyStorePath;

  // The name of the key store type we are using.
  private String keyStoreType;

  //Size of buffer that holds the keytool prompt.
  private static final int  INPUT_BUFSIZE = 1024;

  //How long to pause waiting for the keytool command to echo the password
  //prompt.
  private static final int MILLI_SEC_PAUSE = 250;

  //The line separator character.
  private String lineSeparator;

  //Regular expression matching the keytool prompt for the keystore password.
  private static final String KEYSTORE_PWD_MATCH = "[\\w\\-]*[password:].*";

  //Regular expression matching the keytool prompt for the key password.
  private static final String KEY_PWD_MATCH = "[\\w\\s]*" + "<(.*)>" +
                                              "[:\\w\\s\\(]*password\\):\\s*";

  //Regular expression matching the keytool prompt for import of a
  //certificate.
  private static final String CERT_ADDED_MATCH =
                                  "Certificate was added to keystore[\\s]*";

  //Work-around for AIX Preferences bug that echoes a WARNING to the
  //screen before the password prompt.
  private static final String AIX_PREFERENCES_BUG = "[\\w\\s:,]*" +
  "java\\.util\\.prefs\\.FileSystemPreferences" + "[\\$\\d\\s]*run\\s*" +
  "WARNING: Prefs file removed in background" + "(.)*\\s*" +
  "Enter keystore password:.*";

  static
  {
    String keytoolCommand = null;

    try
    {
      String cmd = System.getProperty("java.home") + File.separator + "bin" +
                   File.separator + "keytool";
      File cmdFile = new File(cmd);
      if (cmdFile.exists())
      {
        keytoolCommand = cmdFile.getAbsolutePath();
      }
      else
      {
        cmd = cmd + ".exe";
        cmdFile = new File(cmd);
        if (cmdFile.exists())
        {
          keytoolCommand = cmdFile.getAbsolutePath();
        }
        else
        {
          keytoolCommand = null;
        }
      }
    }
    catch (Exception e)
    {
      keytoolCommand = null;
    }

    KEYTOOL_COMMAND = SetupUtils.getScriptPath(keytoolCommand);
  }



  /**
   * Indicates whether it is possible to use this certificate manager code to
   * perform operations which may alter the contents of a key store.
   *
   * @return  {@code true} if it appears that the keytool utility is available
   *          and may be used to execute commands that may alter the contents of
   *          a key store, or {@code false} if not.
   */
  public static boolean mayUseCertificateManager()
  {
    return (KEYTOOL_COMMAND != null);
  }



  /**
   * Creates a new certificate manager instance with the provided information.
   *
   * @param  keyStorePath  The path to the key store file, or "NONE" if the key
   *                       store type is "PKCS11".  For the other key store
   *                       types, the file does not need to exist if a new
   *                       self-signed certificate or certificate signing
   *                       request is to be generated, although the directory
   *                       containing the file must exist.  The key store file
   *                       must exist if import or export operations are to be
   *                       performed.
   * @param  keyStoreType  The key store type to use.  It should be one of
   *                       {@code KEY_STORE_TYPE_JKS},
   *                       {@code KEY_STORE_TYPE_JCEKS},
   *                       {@code KEY_STORE_TYPE_PKCS11}, or
   *                       {@code KEY_STORE_TYPE_PKCS12}.
   * @param  keyStorePIN   The PIN required to access the key store.  It must
   *                       not be {@code null}.
   *
   * @throws  IllegalArgumentException  If any of the provided arguments is
   *                                    invalid.
   *
   * @throws  NullPointerException  If any of the provided arguments is
   *                                {@code null}.
   *
   * @throws  UnsupportedOperationException  If it is not possible to use the
   *                                         certificate manager on the
   *                                         underlying platform.
   */
  public CertificateManager(String keyStorePath, String keyStoreType,
                            String keyStorePIN)
         throws IllegalArgumentException, NullPointerException,
                UnsupportedOperationException
  {
    if ((keyStorePath == null) || (keyStorePath.length() == 0))
    {
      throw new NullPointerException("keyStorePath");
    }
    else if ((keyStoreType == null) || (keyStoreType.length() == 0))
    {
      throw new NullPointerException("keyStoreType");
    }
    else if ((keyStorePIN == null) || (keyStorePIN.length() == 0))
    {
      throw new NullPointerException("keyStorePIN");
    }


    if (keyStoreType.equals(KEY_STORE_TYPE_PKCS11))
    {
      if (! keyStorePath.equals(KEY_STORE_PATH_PKCS11))
      {
        // FIXME -- Make this an internationalizeable string.
        throw new IllegalArgumentException("Invalid key store path for " +
                                           "PKCS11 keystore -- it must be " +
                                           KEY_STORE_PATH_PKCS11);
      }
    }
    else if (keyStoreType.equals(KEY_STORE_TYPE_JKS) ||
        keyStoreType.equals(KEY_STORE_TYPE_JCEKS) ||
             keyStoreType.equals(KEY_STORE_TYPE_PKCS12))
    {
      File keyStoreFile = new File(keyStorePath);
      if (keyStoreFile.exists())
      {
        if (! keyStoreFile.isFile())
        {
          // FIXME -- Make this an internationalizeable string.
          throw new IllegalArgumentException("Key store path " + keyStorePath +
                                             " exists but is not a file.");
        }
      }
      else
      {
        File keyStoreDirectory = keyStoreFile.getParentFile();
        if ((keyStoreDirectory == null) || (! keyStoreDirectory.exists()) ||
            (! keyStoreDirectory.isDirectory()))
        {
          // FIXME -- Make this an internationalizeable string.
          throw new IllegalArgumentException("Parent directory for key " +
                         "store path " + keyStorePath + " does not exist or " +
                         "is not a directory.");
        }
      }
    }
    else
    {
      // FIXME -- Make this an internationalizeable string.
      throw new IllegalArgumentException("Invalid key store type -- it must " +
                  "be one of " + KEY_STORE_TYPE_JKS + ", " +
                  "be one of " + KEY_STORE_TYPE_JCEKS + ", " +
                  KEY_STORE_TYPE_PKCS11 + ", or " + KEY_STORE_TYPE_PKCS12);
    }


    this.keyStorePath = keyStorePath;
    this.keyStoreType = keyStoreType;
    this.keyStorePIN  = keyStorePIN;

    keyStore = null;
    String lineSep=System.getProperty("line.separator");
    if(lineSep == null)
      lineSep="\n";
    lineSeparator = lineSep;
  }



  /**
   * Indicates whether the provided alias is in use in the key store.
   *
   * @param  alias  The alias for which to make the determination.  It must not
   *                be {@code null} or empty.
   *
   * @return  {@code true} if the key store exist and already contains a
   *          certificate with the given alias, or {@code false} if not.
   *
   * @throws  KeyStoreException  If a problem occurs while attempting to
   *                             interact with the key store.
   *
   * @throws  NullPointerException  If the provided alias is {@code null} or a
   *                                zero-length string.
   */
  public boolean aliasInUse(String alias)
         throws KeyStoreException, NullPointerException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }


    KeyStore keyStore = getKeyStore();
    if (keyStore == null)
    {
      return false;
    }

    return keyStore.containsAlias(alias);
  }



  /**
   * Retrieves the aliases of the certificates in the specified key store.
   *
   * @return  The aliases of the certificates in the specified key store, or
   *          {@code null} if the key store does not exist.
   *
   * @throws  KeyStoreException  If a problem occurs while attempting to
   *                             interact with the key store.
   */
  public String[] getCertificateAliases()
         throws KeyStoreException
  {
    KeyStore keyStore = getKeyStore();
    if (keyStore == null)
    {
      return null;
    }

    Enumeration<String> aliasEnumeration = keyStore.aliases();
    if (aliasEnumeration == null)
    {
      return new String[0];
    }

    ArrayList<String> aliasList = new ArrayList<String>();
    while (aliasEnumeration.hasMoreElements())
    {
      aliasList.add(aliasEnumeration.nextElement());
    }


    String[] aliases = new String[aliasList.size()];
    return aliasList.toArray(aliases);
  }



  /**
   * Retrieves the certificate with the specified alias from the key store.
   *
   * @param  alias  The alias of the certificate to retrieve.  It must not be
   *                {@code null} or empty.
   *
   * @return  The requested certificate, or {@code null} if the specified
   *          certificate does not exist.
   *
   * @throws  KeyStoreException  If a problem occurs while interacting with the
   *                             key store, or the key store does not exist.
   *
   * @throws  NullPointerException  If the provided alias is {@code null} or a
   *                                zero-length string.
   */
  public Certificate getCertificate(String alias)
         throws KeyStoreException, NullPointerException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }

    KeyStore keyStore = getKeyStore();
    if (keyStore == null)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new KeyStoreException("The key store does not exist.");
    }

    return keyStore.getCertificate(alias);
  }



  /**
   * Generates a self-signed certificate using the provided information.
   *
   * @param  alias      The nickname to use for the certificate in the key
   *                    store.  For the server certificate, it should generally
   *                    be "server-cert".  It must not be {@code null} or empty.
   * @param  subjectDN  The subject DN to use for the certificate.  It must not
   *                    be {@code null} or empty.
   * @param  validity   The length of time in days that the certificate should
   *                    be valid, starting from the time the certificate is
   *                    generated.  It must be a positive integer value.
   *
   * @throws  IllegalArgumentException  If the validity is not positive.
   *
   * @throws  KeyStoreException  If a problem occurs while actually attempting
   *                             to generate the certificate in the key store.
   *
   * @throws  NullPointerException  If either the alias or subject DN is null or
   *                                a zero-length string.
   *
   * @throws  UnsupportedOperationException  If it is not possible to use the
   *                                         keytool utility to alter the
   *                                         contents of the key store.
   */
  public void generateSelfSignedCertificate(String alias, String subjectDN,
                                            int validity)
         throws KeyStoreException, IllegalArgumentException,
                NullPointerException, UnsupportedOperationException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }
    else if ((subjectDN == null) || (subjectDN.length() == 0))
    {
      throw new NullPointerException("subjectDN");
    }
    else if (validity <= 0)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new IllegalArgumentException("The validity must be positive.");
    }

    if (KEYTOOL_COMMAND == null)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new UnsupportedOperationException("The certificate manager may " +
                     "not be used to alter the contents of key stores on " +
                     "this system.");
    }

    if (aliasInUse(alias))
    {
      // FIXME -- Make this an internationalizeable string.
      throw new IllegalArgumentException("A certificate with alias " + alias +
                                         " already exists in the key store.");
    }


    // Clear the reference to the key store, since it will be altered by
    // invoking the KeyTool command.
    keyStore = null;

    // First, we need to run with the "-genkey" command to create the private
    // key.
    String[] commandElements =
    {
      KEYTOOL_COMMAND,
      getGenKeyCommand(),
      "-alias", alias,
      "-dname", subjectDN,
      "-keyalg", "rsa",
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, true);

    // Next, we need to run with the "-selfcert" command to self-sign the
    // certificate.
    commandElements = new String[]
    {
      KEYTOOL_COMMAND,
      "-selfcert",
      "-alias", alias,
      "-validity", String.valueOf(validity),
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, false);
  }



  /**
   * Generates a certificate signing request (CSR) using the provided
   * information.
   *
   * @param  alias      The nickname to use for the certificate in the key
   *                    store.  For the server certificate, it should generally
   *                    be "server-cert".  It must not be {@code null} or empty.
   * @param  subjectDN  The subject DN to use for the certificate.  It must not
   *                    be {@code null} or empty.
   *
   * @return  The file containing the generated certificate signing request.
   *
   * @throws  KeyStoreException  If a problem occurs while actually attempting
   *                             to generate the private key in the key store or
   *                             generate the certificate signing request based
   *                             on that key.
   *
   * @throws  IOException  If a problem occurs while attempting to create the
   *                       file to which the certificate signing request will be
   *                       written.
   *
   * @throws  NullPointerException  If either the alias or subject DN is null or
   *                                a zero-length string.
   *
   * @throws  UnsupportedOperationException  If it is not possible to use the
   *                                         keytool utility to alter the
   *                                         contents of the key store.
   */
  public File generateCertificateSigningRequest(String alias, String subjectDN)
         throws KeyStoreException, IOException, NullPointerException,
                UnsupportedOperationException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }
    else if ((subjectDN == null) || (subjectDN.length() == 0))
    {
      throw new NullPointerException("subjectDN");
    }

    if (KEYTOOL_COMMAND == null)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new UnsupportedOperationException("The certificate manager may " +
                     "not be used to alter the contents of key stores on " +
                     "this system.");
    }

    if (aliasInUse(alias))
    {
      // FIXME -- Make this an internationalizeable string.
      throw new IllegalArgumentException("A certificate with alias " + alias +
                                         " already exists in the key store.");
    }


    // Clear the reference to the key store, since it will be altered by
    // invoking the KeyTool command.
    keyStore = null;


    // First, we need to run with the "-genkey" command to create the private
    // key.
    String[] commandElements =
    {
      KEYTOOL_COMMAND,
      getGenKeyCommand(),
      "-alias", alias,
      "-dname", subjectDN,
      "-keyalg", "rsa",
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, true);

    // Next, we need to run with the "-certreq" command to generate the
    // certificate signing request.
    File csrFile = File.createTempFile("CertificateManager-", ".csr");
    csrFile.deleteOnExit();
    commandElements = new String[]
    {
      KEYTOOL_COMMAND,
      "-certreq",
      "-alias", alias,
      "-file", csrFile.getAbsolutePath(),
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, false);

    return csrFile;
  }



  /**
   * Adds the provided certificate to the key store.  This may be used to
   * associate an externally-signed certificate with an existing private key
   * with the given alias.
   *
   * @param  alias            The alias to use for the certificate.  It must not
   *                          be {@code null} or empty.
   * @param  certificateFile  The file containing the encoded certificate.  It
   *                          must not be {@code null}, and the file must exist.
   *
   * @throws  IllegalArgumentException  If the provided certificate file does
   *                                    not exist.
   *
   * @throws  KeyStoreException  If a problem occurs while interacting with the
   *                             key store.
   *
   * @throws  NullPointerException  If the provided alias is {@code null} or a
   *                                zero-length string, or the certificate file
   *                                is {@code null}.
   *
   * @throws  UnsupportedOperationException  If it is not possible to use the
   *                                         keytool utility to alter the
   *                                         contents of the key store.
   */
  public void addCertificate(String alias, File certificateFile)
         throws IllegalArgumentException, KeyStoreException,
                NullPointerException, UnsupportedOperationException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }

    if (certificateFile == null)
    {
      throw new NullPointerException("certificateFile");
    }
    else if ((! certificateFile.exists()) ||
             (! certificateFile.isFile()))
    {
      // FIXME -- Make this an internationalizeable string.
      throw new IllegalArgumentException("Certificate file " +
                                         certificateFile.getAbsolutePath() +
                                         " does not exist or is not a file.");
    }

    if (KEYTOOL_COMMAND == null)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new UnsupportedOperationException("The certificate manager may " +
                     "not be used to alter the contents of key stores on " +
                     "this system.");
    }


    // Clear the reference to the key store, since it will be altered by
    // invoking the KeyTool command.
    keyStore = null;


    String[] commandElements =
    {
      KEYTOOL_COMMAND,
      "-import",
      "-noprompt",
      "-alias", alias,
      "-file", certificateFile.getAbsolutePath(),
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, true);
  }


  /**
   * Removes the specified certificate from the key store.
   *
   * @param  alias  The alias to use for the certificate to remove. It must not
   *                be {@code null} or an empty string, and it must exist in
   *                the key store.
   *
   * @throws  IllegalArgumentException  If the specified certificate does not
   *                                    exist in the key store.
   *
   * @throws  KeyStoreException  If a problem occurs while interacting with the
   *                             key store.
   *
   * @throws  NullPointerException  If the provided alias is {@code null} or a
   *                                zero-length string, or the certificate file
   *                                is {@code null}.
   *
   * @throws  UnsupportedOperationException  If it is not possible to use the
   *                                         keytool utility to alter the
   *                                         contents of the key store.
   */
  public void removeCertificate(String alias)
         throws IllegalArgumentException, KeyStoreException,
                NullPointerException, UnsupportedOperationException
  {
    if ((alias == null) || (alias.length() == 0))
    {
      throw new NullPointerException("alias");
    }

    if (KEYTOOL_COMMAND == null)
    {
      // FIXME -- Make this an internationalizeable string.
      throw new UnsupportedOperationException("The certificate manager may " +
                     "not be used to alter the contents of key stores on " +
                     "this system.");
    }

    if (! aliasInUse(alias))
    {
      // FIXME -- Make this an internationalizeable string.
     throw new IllegalArgumentException("There is no certificate with alias " +
                                         alias + " in the key store.");
    }


    // Clear the reference to the key store, since it will be altered by
    // invoking the KeyTool command.
    keyStore = null;


    String[] commandElements =
    {
      KEYTOOL_COMMAND,
      "-delete",
      "-alias", alias,
      "-keystore", keyStorePath,
      "-storetype", keyStoreType
    };
    runKeyTool(commandElements, keyStorePIN, false);
  }


/**
 * Run the keytool command and try to echo the password when a password
 * prompt is matched.
 *
 * @param args The keytool args.
 * @param password The password.
 * @param multiPrompt Some keytool commands have multiple prompts, this flag
 *                    causes another code path to be take in those cases.
 *
 * @throws KeyStoreException If an error occurred.
 */
  private void runKeyTool(String[] args, String password, boolean multiPrompt)
  throws KeyStoreException {
    boolean noWait;
    char[] buf = new char[INPUT_BUFSIZE];
    int retBytes, exitValue;
    ProcessBuilder procBldr = new ProcessBuilder(args);
    StringBuilder keytoolPromptStrings = new StringBuilder();

    try {
      procBldr.redirectErrorStream(true);
      Process process = procBldr.start();
      InputStream is = process.getInputStream();
      OutputStream os = process.getOutputStream();
      BufferedReader keytoolRdr = new BufferedReader(new InputStreamReader(is));
      waitForPrompt(keytoolRdr);
      while((retBytes=keytoolRdr.read(buf, 0, INPUT_BUFSIZE)) != -1) {
        String promptStr = new String(buf, 0, retBytes);
        keytoolPromptStrings.append(promptStr);
        if(multiPrompt)
          noWait = multiPrompt(promptStr, os, password);
        else
          noWait = singlePrompt(promptStr,os, password);
        //The noWait boolean is true if a terminating keytool prompt was seen
        //and processing should stop.
        if(noWait)
          break;
        waitForPrompt(keytoolRdr);
      }
      os.close();
      process.waitFor();
      exitValue = process.exitValue();
      if (exitValue != 0)
        processFailure(exitValue, keytoolPromptStrings);
    }  catch (InterruptedException interEx) {
      throw new KeyStoreException(interEx.getMessage());
    } catch (IOException ioEx) {
      throw new KeyStoreException(ioEx.getMessage());
    }
  }

/**
 * Process a failure return code from the keytool execution.
 *
 * @param rc The return code of the keytool process.
 * @param bldr A string builder to build the error message.
 *
 * @throws KeyStoreException If the error message cannot be built for some
 *                           reason.
 */
  private void processFailure(int rc, StringBuilder bldr)
  throws KeyStoreException {
    StringBuilder message = new StringBuilder();
    message.append("Unexpected exit code of ");
    message.append(rc);
    message.append(" returned from the keytool utility.");
    if (bldr.length() != 0) {
      message.append("  The generated output was:  '");
      message.append(bldr.toString());
      message.append("'.");
    }
    throw new KeyStoreException(message.toString());
  }

  private boolean singlePrompt(String inputStr, OutputStream os, String pwd)
  throws IOException, KeyStoreException  {
    if(inputStr.matches(KEYSTORE_PWD_MATCH)) {
      os.write(pwd.getBytes());
      os.write(lineSeparator.getBytes());
      os.flush();
    } else if(inputStr.matches(AIX_PREFERENCES_BUG)) {
        os.write(pwd.getBytes());
        os.write(lineSeparator.getBytes());
        os.flush();
    } else
      throw new KeyStoreException("Single prompt didn't match: " + inputStr);
    return true;
  }

  /**
   * Process a multi-prompt keytool command (-genkeypair and -import options).
   *
   * @param inputStr The prompt string from the keytool command.
   * @param os The output stream to echo the password to.
   * @param pwd The password.
   * @return True if execution will continue. Some of the prompts are the last
   *         prompt keytool echos.
   *
   * @throws IOException If an IO error occurred.
   * @throws KeyStoreException If a input stream didn't match.
   */
  private boolean multiPrompt(String inputStr, OutputStream os, String pwd)
  throws IOException, KeyStoreException  {
    boolean retVal=false;
    if(inputStr.matches(KEYSTORE_PWD_MATCH)) {
      os.write(pwd.getBytes());
      os.write(lineSeparator.getBytes());
      os.flush();
    } else if(inputStr.matches(KEY_PWD_MATCH)) {
      retVal=true;
      os.write(lineSeparator.getBytes());
      os.flush();
    } else if(inputStr.matches(CERT_ADDED_MATCH))
      retVal = true;
    else if(inputStr.matches(AIX_PREFERENCES_BUG)) {
        os.write(pwd.getBytes());
        os.write(lineSeparator.getBytes());
        os.flush();
    } else
      throw new KeyStoreException("Multi-prompt didn't match: " + inputStr);
    return retVal;
  }

  /**
   * Loop waiting for the keytool command to echo a prompt.
   *
   * @param rdr The reader the keytool command will echo the prompt to.
   *
   * @throws IOException If an IO error occurred.
   * @throws InterruptedException If the keytool command was interrupted.
   */
  private void waitForPrompt(BufferedReader rdr)
  throws IOException, InterruptedException {
    while(!rdr.ready())
        Thread.sleep(MILLI_SEC_PAUSE);
  }



  /**
   * Retrieves a handle to the key store.
   *
   * @return  The handle to the key store, or {@code null} if the key store
   *          doesn't exist.
   *
   * @throws  KeyStoreException  If a problem occurs while trying to open the
   *                             key store.
   */
  private KeyStore getKeyStore()
          throws KeyStoreException
  {
    if (keyStore != null)
    {
      return keyStore;
    }

    // For JKS, JCEKS and PKCS12 key stores, we should make sure the file
    // exists, and we'll need an input stream that we can use to read it.
    // For PKCS11 key stores there won't be a file and the input stream should
    // be null.
    FileInputStream keyStoreInputStream = null;
    if (keyStoreType.equals(KEY_STORE_TYPE_JKS) ||
        keyStoreType.equals(KEY_STORE_TYPE_JCEKS) ||
        keyStoreType.equals(KEY_STORE_TYPE_PKCS12))
    {
      File keyStoreFile = new File(keyStorePath);
      if (! keyStoreFile.exists())
      {
        return null;
      }

      try
      {
        keyStoreInputStream = new FileInputStream(keyStoreFile);
      }
      catch (Exception e)
      {
        throw new KeyStoreException(String.valueOf(e), e);
      }
    }


    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
    try
    {
      keyStore.load(keyStoreInputStream, keyStorePIN.toCharArray());
      return this.keyStore = keyStore;
    }
    catch (Exception e)
    {
      throw new KeyStoreException(String.valueOf(e), e);
    }
    finally
    {
      if (keyStoreInputStream != null)
      {
        try
        {
          keyStoreInputStream.close();
        }
        catch (Throwable t)
        {
        }
      }
    }
  }

  /**
   * Returns whether we are running JDK 1.5 or not.
   * @return <CODE>true</CODE> if we are running JDK 1.5 and <CODE>false</CODE>
   * otherwise.
   */
  private boolean isJDK15()
  {
    boolean isJDK15 = false;
    try
    {
      String javaRelease = System.getProperty ("java.version");
      isJDK15 = javaRelease.startsWith("1.5");
    }
    catch (Throwable t)
    {
      System.err.println("Cannot get the java version: " + t);
    }
    return isJDK15;
  }

  private String getGenKeyCommand()
  {
    String genKeyCommand;
    if (!isJDK15())
    {
      genKeyCommand = "-genkeypair";
    }
    else
    {
      genKeyCommand = "-genkey";
    }
    return genKeyCommand;
  }
}


