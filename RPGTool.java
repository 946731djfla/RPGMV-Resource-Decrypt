import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RPGTool {
    private boolean eKey = false;
    private boolean overWrite = false;

    //decorate
    private boolean thread = false;
    private boolean threadLock = false;
    private boolean autoNumber = false;
    private int threadNumber = 0;

    private byte[] enHeader = {0x52, 0x50, 0x47, 0x4D, 0x56, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
    private byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
    private byte[] oggHeader = {0x4F, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE0, 0x4B};
    private byte[] m4aHeader = {0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x4D, 0x34, 0x41, 0x20, 0x00, 0x00, 0x02, 0x00};

    private int[] keys;
    private ArrayList<File> list = new ArrayList<File>();

    RPGTool(boolean overWrite) {
        this.overWrite = overWrite;
    }

    RPGTool(String key) throws Exception {
        if (key.length() == 0 || key.length() % 2 != 0)
            throw new Exception("Key error");
        int keyLength = key.length() / 2;
        this.eKey = true;
        this.keys = new int[keyLength];
        for (int i = 0; i < keyLength; i++) {
            String singleKey = key.substring(0, 2);
            this.keys[i] = Integer.parseInt(singleKey, 16);
            key = key.substring(2);
        }
    }
    
    RPGTool(String key, boolean overWrite) throws Exception {
        this(key);
        this.overWrite = overWrite;
    }

    private void inspectFile(File file) throws Exception {
        if (!file.exists() || !file.isFile() || !file.canRead())
            throw new Exception("addFile error! -> \n1.File not exists\n2.File is folder\n3.File not read");
    }

    private void inspectIndex(int index) throws Exception {
        inspectSize();
        if (index < 0)
            throw new Exception("index < 0");
        if (index > list.size())
            throw new Exception("index > size");
    }

    private void inspectSize() throws Exception {
        if (list.size() == 0)
            throw new Exception("size == 0");
    }


    private byte[] getDeHeader(File file) throws Exception {
        String name = file.getName();
        int index = name.lastIndexOf(".") + 1;
        String suffix = name.substring(index);
        switch (suffix) {
            case "rpgmvp":
            case "png_":
                return pngHeader;
            case "rpgmvm":
            case "m4a_":
                return m4aHeader;
            case "rpgmvo":
            case "ogg_":
                return oggHeader;
            default:
                throw new Exception("Unknown suffix -> " + suffix);
        }
    }

    private File getFile(File file) throws Exception {
        String name = file.getName();
        String path = file.getParent();
        int index = name.lastIndexOf(".") + 1;
        String suffix = name.substring(index);
        String re = path + "/" + name.substring(0, index);
        switch (suffix) {
            case "png":
                return new File(re + "rpgmvp");
            case "m4a":
                return new File(re + "rpgmvm");
            case "ogg":
                return new File(re + "rpgmvo");
            case "rpgmvp":
            case "png_":
                return new File(re + "png");
            case "rpgmvm":
            case "m4a_":
                return new File(re + "m4a");
            case "rpgmvo":
            case "ogg_":
                return new File(re + "ogg");
            default:
                throw new Exception("Unknown suffix -> " + suffix);
        }
    }
    
    void openThread(boolean s) {
        this.thread = s;
    }

    void openAutoThreadNumber(boolean s) {
        this.autoNumber = s;
    }

    void openThreadLock(boolean s) {
        this.threadLock = s;
    }

    void setThreadNumber(int number) throws Exception {
        if (!thread)
            throw new Exception("thread has not open");
        if (autoNumber)
            throw new Exception("has open auto thread number");
        if (number < 0)
            throw new Exception("thread number < 0");
        this.threadNumber = number;
    }

    FileList getFilelist() {
        return new FileList();
    }

    class FileList {

        Encrypt getEncrypt() throws Exception {
            if (!eKey)
                throw new Exception("not Key");
            inspectSize();
            return new Encrypt();
        }

        Decrypt getDecrypt() throws Exception {
            inspectSize();
            return new Decrypt();
        }

        int getSize() {
            return list.size();
        }

        boolean addFile(File file) throws Exception {
            inspectFile(file);
            return list.add(file);
        }

        void addFiles(File[] files) throws Exception {
            for (File file : files) {
                addFile(file);
            }
        }

        void addFiles(ArrayList<File> files) throws Exception {
            addFiles(files.toArray(new File[files.size()]));
        }

        void setFile(int index, File file) throws Exception {
            inspectFile(file);
            inspectIndex(index);
            list.set(index, file);
        }

        void removeFile(int index) throws Exception {
            inspectIndex(index);
            list.remove(index);
        }

    }

    class Encrypt {
        private boolean en(File file) throws Exception {
            File outFile = getFile(file);
            if (!overWrite && outFile.exists()) {
                return false;
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] Header = new byte[keys.length];
            fis.read(Header);
            for (int i = 0; i < Header.length; i++)
                Header[i] ^= keys[i];
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(enHeader);
            fos.write(Header);
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        }

        boolean enFile() throws Exception {
            inspectSize();
            return en(list.get(0));
        }

        boolean enFile(int index) throws Exception {
            inspectIndex(index);
            return en(list.get(index));
        }

        boolean enFiles(int index, int toindex) throws Exception {
            inspectIndex(index);
            if (toindex < 0 || index > toindex)
                throw new Exception("toindex < 0 || index > toindex");
            if (toindex > list.size())
                throw new Exception("exceeding list size");
            List<File> sublist;
            if (index == 0 & toindex == list.size())
                sublist = list;
            else
                sublist = list.subList(index, toindex);
            int max = sublist.size();
            int i = 0;
            for (File file : sublist) {
                i += en(file) ? 1 : 0;
            }
            return max == i ? true : false;
        }

        boolean enAllFile() throws Exception {
            return enFiles(0, list.size());
        }
    }

    class Decrypt {
        private boolean de(File file) throws Exception {
            File outFile = getFile(file);
            if (!overWrite && outFile.exists()) {
                return false;
            }
            FileInputStream fis = new FileInputStream(file);
            fis.skip(16);
            byte[] deHeader;
            if (eKey) {
                deHeader = new byte[keys.length];
                fis.read(deHeader);
                for (int i = 0; i < deHeader.length; i++)
                    deHeader[i] ^= keys[i];
            } else {
                fis.skip(16);
                deHeader = getDeHeader(file);
            }
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(deHeader);
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        }

        boolean deFile() throws Exception {
            inspectSize();
            return de(list.get(0));
        }

        boolean deFile(int index) throws Exception {
            inspectIndex(index);
            return de(list.get(index));
        }

        boolean deFiles(int index, int toindex) throws Exception {
            inspectIndex(index);
            if (toindex < 0 || index > toindex)
                throw new Exception("toindex < 0 || index > toindex");
            if (toindex > list.size())
                throw new Exception("exceeding list size");
            List<File> sublist;
            if (index == 0 & toindex == list.size())
                sublist = list;
            else
                sublist = list.subList(index, toindex);
            int max = sublist.size();
            int i = 0;
            for (File file : sublist) {
                i += de(file) ? 1 : 0;
            }
            return max == i ? true : false;
        }

        boolean deAllFile() throws Exception {
            return deFiles(0, list.size());
        }
    }
}
