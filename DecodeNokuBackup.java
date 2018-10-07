import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.spec.KeySpec;
import java.util.ArrayList;

public class DecodeNokuBackup {
    public static void main(String[] args) throws Exception {
        int numTotalWallets = 0;
        int numStoredWallets = 0;
        int numViewWallets = 0;
        int numStealthSpaces = 0;
        int numStealthWallets = 0;
        boolean success = false;
        ArrayList<String> storedWallets = new ArrayList<>();
        ArrayList<String> viewWallets = new ArrayList<>();
        ArrayList<String> storedWalletsStealth = new ArrayList<>();
        ArrayList<String> viewWalletsStealth = new ArrayList<>();


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("NOKU DECODE BACKUP TOOL");
        System.out.println("Enter the name of the backup file (place it in the same directory of this tool): ");
        String fileName = br.readLine();
        String currentProgramPath = new File(".").getCanonicalPath();
        fileName = currentProgramPath + File.separator + fileName;
        File backupFile = new File(fileName);
        System.out.println("Enter the password: ");
        String password = br.readLine();

        try {
            FileInputStream in = new FileInputStream(backupFile);

            byte[] salt2 = new byte[8], iv2 = new byte[128 / 8];
            in.read(salt2);
            in.read(iv2);
            SecretKeyFactory factory2 =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec2 = new PBEKeySpec(password.toCharArray(), salt2, 10000, 128);
            SecretKey tmp2 = factory2.generateSecret(spec2);
            SecretKeySpec skey2 = new SecretKeySpec(tmp2.getEncoded(), "AES");

            Cipher ci2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci2.init(Cipher.DECRYPT_MODE, skey2, new IvParameterSpec(iv2));


            try (FileOutputStream out2 = new FileOutputStream(currentProgramPath + File.separator + "backup.noku_dec")) {
                processFile(ci2, in, out2);
            }
            

            File backup = new File(currentProgramPath + File.separator + "backup.noku_dec");


            try (
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(backup));

            ) {

                byte[] buffer = new byte[(int) backup.length()];
                inputStream.read(buffer);
                int numWallets;
                String tmp = "";
                int i = 0;

                while (buffer[i] != '_') {
                    tmp += (char) (buffer[i] & 0xFF);
                    i++;
                }

                numWallets = Integer.parseInt(tmp);

                if (numWallets != 0) {

                    int[] lengths = new int[numWallets];
                    int c = 0;
                    i++;

                    while (buffer[i] != 'W' && buffer[i] != '<') {
                        String tmpLength = "";
                        while (buffer[i] != '_') {
                            tmpLength += (char) (buffer[i] & 0xFF);
                            i++;
                        }
                        lengths[c++] = Integer.parseInt(tmpLength);
                        i++;
                    }

                    for (int j = 0; j < numWallets; j++) {
                        while (i < buffer.length && buffer[i] != '0' && buffer[i] != '<') {
                            i++;
                        }

                        if (i < buffer.length) {
                            if (buffer[i] == '0') {
                                String address = "";
                                while (buffer[i] != '\t') {
                                    address += (char) (buffer[i] & 0xFF);
                                    i++;
                                }

                                numStoredWallets++;
                                storedWallets.add(address);


                                i++;
                                int k = 0;
                                while (k < lengths[j]) {
                                    k++;
                                }


                                i = i + k;

                            } else if (buffer[i] == '<') {
                                while (buffer[i] != '0') {
                                    i++;
                                }

                                String address = "";
                                while (buffer[i] != '\t') {
                                    address += (char) (buffer[i] & 0xFF);
                                    i++;
                                }


                                numViewWallets++;
                                viewWallets.add(address);


                            }
                        }
                    }

                    if (buffer[i] == '\t')
                        i++;

                    char charTmp = (char) (buffer[i] & 0xFF);
                    int numOfStealthSpaces = Character.getNumericValue(charTmp);
                    i += 2;
                    c = 0;
                    if (numOfStealthSpaces != 0) {
                        int[] stealthSpacesHowManyWallets = new int[numOfStealthSpaces];
                        while (buffer[i] != 'S' && c < numOfStealthSpaces) {
                            String tmpLength = "";
                            while (buffer[i] != '_') {
                                tmpLength += (char) (buffer[i] & 0xFF);
                                i++;
                            }

                            stealthSpacesHowManyWallets[c++] = Integer.parseInt(tmpLength);
                            i++;
                        }


                        for (int z = 0; z < numOfStealthSpaces; z++) {

                            i += 10;

                            boolean shouldAddStealthSpace = true;

                            String id = "";
                            while (buffer[i] != ' ') {
                                id += (char) (buffer[i] & 0xFF);
                                i++;
                            }

                            i++;

                            String hashPass = "";

                            while (buffer[i] != '_') {
                                hashPass += (char) (buffer[i] & 0xFF);
                                i++;
                            }


                            numStealthSpaces++;


                            i++;

                            int[] lengthsSt = new int[stealthSpacesHowManyWallets[z]];
                            int cont = 0;

                            while (cont < stealthSpacesHowManyWallets[z] && buffer[i] != 'W' && buffer[i] != '<') {  //&& buffer[i] != '<'
                                String tmpLength = "";
                                while (buffer[i] != '_') {
                                    tmpLength += (char) (buffer[i] & 0xFF);
                                    i++;
                                }
                                lengthsSt[cont++] = Integer.parseInt(tmpLength);
                                i++;
                            }


                            for (int h = 0; h < stealthSpacesHowManyWallets[z]; h++) {


                                while (i < buffer.length && buffer[i] != '0' && buffer[i] != '<') {
                                    i++;
                                }

                                if (buffer[i] == '0') {
                                    String address = "";
                                    while (buffer[i] != '\t') {
                                        address += (char) (buffer[i] & 0xFF);
                                        i++;
                                    }


                                    storedWalletsStealth.add(address);


                                    i++;
                                    int k = 0;
                                    while (k < lengthsSt[h]) {
                                        k++;
                                    }


                                    i = i + k;


                                } else if (buffer[i] == '<') {
                                    while (buffer[i] != '0') {
                                        i++;
                                    }

                                    String address = "";
                                    while (buffer[i] != '\t') {
                                        address += (char) (buffer[i] & 0xFF);
                                        i++;
                                    }

                                    viewWalletsStealth.add(address);


                                    i++;


                                }


                            }

                        }


                    }

                    System.out.println("Backup decrypted");
                    success = true;

                } else {
                    System.out.println("Backup was empty");
                    success = false;


                }


            } catch (IOException ex) {
                ex.printStackTrace();
            }


            if (new File(currentProgramPath + File.separator + "backup.noku_dec").exists())
                new File(currentProgramPath + File.separator + "backup.noku_dec").delete();


        } catch (Exception e) {
            System.out.println("Wrong password or wrong file");
            success = false;

            e.printStackTrace();
        }

        if (success) {
            System.out.println("");
            System.out.println("Number of stored wallets (with priv key): " + numStoredWallets);
            System.out.println("");
            System.out.println("STORED WALLETS ADDRESSES: ");
            for (int i = 0; i < storedWallets.size(); i++) {
                System.out.println(storedWallets.get(i));
            }

            System.out.println("");
            System.out.println("Number of view wallets (without priv key): " + numViewWallets);
            System.out.println("");
            System.out.println("VIEW WALLETS ADDRESSES: ");
            for (int i = 0; i < viewWallets.size(); i++) {
                System.out.println(viewWallets.get(i));
            }
            System.out.println("");
            System.out.println("Number of Stealth Spaces " + numStealthSpaces);
            System.out.println("");
            System.out.println("Number of stored wallets (with priv key) in stealth spaces: " + storedWalletsStealth.size());
            System.out.println("");
            System.out.println("STEALTH SPACES STORED WALLETS ADDRESSES: ");
            for (int i = 0; i < storedWalletsStealth.size(); i++) {
                System.out.println(storedWalletsStealth.get(i));
            }

            System.out.println("");
            System.out.println("Number of view wallets (without priv key) in stealth spaces: " + viewWalletsStealth.size());
            System.out.println("");
            System.out.println("STEALTH SPACES VIEW WALLETS ADDRESSES: ");
            for (int i = 0; i < viewWalletsStealth.size(); i++) {
                System.out.println(viewWalletsStealth.get(i));
            }
        }


    }

    private static void processFile(Cipher ci, InputStream in, OutputStream out)
            throws javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException,
            java.io.IOException {
        byte[] ibuf = new byte[1024];
        int len;
        while ((len = in.read(ibuf)) != -1) {
            byte[] obuf = ci.update(ibuf, 0, len);
            if (obuf != null) out.write(obuf);
        }
        byte[] obuf = ci.doFinal();
        if (obuf != null) out.write(obuf);
    }
}
