import java.io.*;
import java.util.*;

public class HuffmanCode {
    private TreeNode[] nodes;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        HuffmanCode huffmanCode = new HuffmanCode();
        huffmanCode.zipFile("C:\\Users\\shokr\\Desktop\\TestHuffmanProject\\test.txt", "C:\\Users\\shokr\\Desktop\\TestHuffmanProject\\ziptest.zip");
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println(elapsedTime);
        //HuffmanCode huffmanCode = new HuffmanCode();
        //Scanner console = new Scanner(System.in);
        //menu(console, huffmanCode);


    }

    public static void menu(Scanner console, HuffmanCode huffmanCode) {
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println();
        System.out.println("1) Compress File");
        System.out.println("2) Decompress File");
        System.out.println("3) Exit");
        System.out.print("Enter Your Choice: ");
        String choice = console.next();
        if (choice.equalsIgnoreCase("1")) {
            System.out.print("Enter the file path that you want to compress: ");
            String pathOfFile = console.next();
            System.out.print("Enter the path of compressed file: ");
            String compressedFilePath = console.next();
            huffmanCode.zipFile(pathOfFile, compressedFilePath);
            menu(console, huffmanCode);
        } else if (choice.equalsIgnoreCase("2")) {
            System.out.print("Enter the file path that you want to decompressed: ");
            String pathOfFile = console.next();
            System.out.print("Enter the path of decompressed file: ");
            String decompressedFilePath = console.next();
            huffmanCode.unZipFile(pathOfFile, decompressedFilePath);
            menu(console, huffmanCode);
        } else if (choice.equalsIgnoreCase("3")) {
            System.out.println("Thanks!");
            System.out.println("-------------------------------------------------------------------------------------------");
        } else {
            System.out.println("Invalid Choice!");
            menu(console, huffmanCode);
        }
    }

    /**
     * By traversing the tree , Character by character translation  *  Time complexity  O(codeArray.length()) better  * * @param nodes  Huffman's tree node  * @param huffmanBytes  Byte array of compressed file  * @return  Original byte array
     */
    private byte[] huffmanUnzip(TreeNode[] nodes, byte[] huffmanBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int huffmanByte : huffmanBytes) {
            int temp = huffmanByte;
            temp |= 256;
            String str = Integer.toBinaryString(temp);
            stringBuilder.append(str.substring(str.length() - 8));
        }
        //  Keep the root node of the tree
        int treeRoot = nodes.length - 1;
        //  The root node of the current subtree
        int root = treeRoot;
        List<Byte> list = new ArrayList<>();
        //  Start translating   decode
        char[] codeArray = stringBuilder.toString().toCharArray();
        for (char c : codeArray) {
            int lchild = nodes[root].lchild;
            int rchild = nodes[root].rchild;
            if (c == '0') {
                root = lchild;
                if (nodes[root].lchild == 0) {
                    list.add(nodes[root].symbol);
                    root = treeRoot;
                }
            } else if (c == '1') {
                root = rchild;
                if (nodes[root].lchild == 0) {

                    list.add(nodes[root].symbol);
                    root = treeRoot;
                }
            }
        }
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    private byte[] huffmanZip(byte[] bytes) {
        // 1. Calculate weight
        Data[] datas = computeWeight(bytes);
        // 2. Construct the Huffman tree
        TreeNode[] nodes = bulidHuffmanTree(datas);
        // 3. Do Huffman coding
        Map<Byte, String> huffmanCodes = createHuffmanCode(nodes, datas.length);
        // 4. Compress file bytes
        return doZip(bytes, huffmanCodes);
    }

    private int[] select2HTNode(TreeNode[] nodes, int nSize) {
        //  Store the smallest weight , First initialize to the maximum number
        int[] minValue = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        //  The subscript of the minimum two weights   Initialize to -1
        int[] min = new int[]{-1, -1};
        for (int i = 1; i < nSize; i++) {
            //  Never have a parent node , Select from the subtree with non-zero weight ( here node[0] The index of 0 Inconvenience , So give up node[0])
            if (nodes[i].parent == 0 && nodes[i].weight != 0) {
                if (nodes[i].weight < minValue[0]) {
                    minValue[1] = minValue[0];
                    minValue[0] = nodes[i].weight;
                    //  Update subscript
                    min[1] = min[0];
                    min[0] = i;
                } else if (nodes[i].weight < minValue[1]) {
                    minValue[1] = nodes[i].weight;
                    min[1] = i;
                }
            }
        }
        return min;
    }


    public void unZipFile(String zipFile, String unZipped) {
        try (
                InputStream reader = new FileInputStream(zipFile);
                ObjectInputStream objectReader = new ObjectInputStream(reader);
                OutputStream outputWriter = new FileOutputStream(unZipped);
        ) {
            byte[] huffmanBytes = (byte[]) objectReader.readObject();
            TreeNode[] nodes = (TreeNode[]) objectReader.readObject();
            byte[] bytes = huffmanUnzip(nodes, huffmanBytes);
            outputWriter.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zipFile(String srcFile, String dstFile) {
        try (
                FileInputStream fis = new FileInputStream(srcFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream os = new FileOutputStream(dstFile);
                ObjectOutputStream oos = new ObjectOutputStream(os);
        ) {
            byte[] bytes = new byte[fis.available()];
            int read = bis.read(bytes);
            byte[] huffmanZipBytes = huffmanZip(bytes);
            oos.writeObject(huffmanZipBytes);
            oos.writeObject(nodes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bytes Source file byte array  * @param huffmanCodes  Huffman code table  * @return byte[]  Compressed new byte array
     */
    private byte[] doZip(byte[] bytes, Map<Byte, String> huffmanCodes) {
        StringBuilder builder = new StringBuilder();
        //  The original byte is translated into Huffman code
        for (byte b : bytes) {
            builder.append(huffmanCodes.get(b));
        }
        int quotient = builder.length() / 8;
        int remainder = builder.length() % 8;
        int length = quotient;
        if (remainder != 0) {
            length = quotient + 1;
        }
        //  Used to store compressed bytes
        byte[] newBytes = new byte[length];
        //  Start compressing
        //  Every eight bit binary string is compressed into one byte
        for (int i = 0; i < quotient; i++) {
            String str = builder.substring(i * 8, (i + 1) * 8);
            newBytes[i] = (byte) Integer.parseInt(str, 2);
        }
        //  Finally, there are not enough eight 0 A filling , Compress again
        if (remainder != 0) {
            builder = new StringBuilder(builder.substring(quotient * 8));
            for (int i = 0; i < 8 - remainder; i++) {
                builder.append(0);
            }
            newBytes[length - 1] = (byte) Integer.parseInt(builder.toString(), 2);
        }
        return newBytes;
    }

    private Map<Byte, String> createHuffmanCode(TreeNode[] nodes, int count) {
        Map<Byte, String> huffmanCodes = new HashMap<Byte, String>();
        //  Used to record the code of the leaf node
        StringBuilder sb = new StringBuilder();
        //  Traverse up from each leaf node , Until the root node
        for (int i = 1; i <= count; i++) {
            //  Get the parent node of the leaf node
            int parent = nodes[i].parent;
            //  Current node index
            int index = i;
            do {
                if (nodes[parent].lchild == index) {
                    //  Collection code
                    sb.append(0);
                }
                if (nodes[parent].rchild == index) {
                    //  Collection code
                    sb.append(1);
                }
                index = parent;
                parent = nodes[index].parent;
            } while (parent != 0);
            //  From bottom to top , Need to flip
            String code = sb.reverse().toString();
            //  Store key value pairs
            huffmanCodes.put(nodes[i].symbol, code);
            //  Empty  StringBuilder  Prepare for the next cycle
            sb.setLength(0);
        }
        return huffmanCodes;
    }

    private Data[] computeWeight(byte[] bytes) {
        //  Calculate the weight of each byte
        Map<Byte, Data> dataMap = new HashMap<>();
        for (byte aByte : bytes) {
            Data data = dataMap.get(aByte);
            if (data == null) {
                Data temp = new Data();
                temp.symbol = aByte;
                temp.weight++;
                dataMap.put(aByte, temp);
            } else {
                data.weight++;
            }
        }
        //  Get the weight array
        return dataMap.values().toArray(new Data[0]);
    }

    /**
     * Building the Huffman tree  * @param data  Weight data  * @return  The tree node of Huffman tree
     */
    private TreeNode[] bulidHuffmanTree(Data[] data) {
        //  The total number of nodes of Huffman tree is ( 2 * data.length - 1 )
        //  In order to determine the parent node of the node ,  and index=0 It's inconvenient , therefore nodes[0] Don't store data
        int length = 2 * data.length;
        //  Create a Huffman tree node array
        // TreeNode[] nodes = new TreeNode[length];
        this.nodes = new TreeNode[length];
        //  Initialize the tree node  symbol  as well as  weight
        for (int i = 0; i < data.length; i++) {
            int index = i + 1;
            nodes[index] = new TreeNode();
            nodes[index].symbol = data[i].symbol;
            nodes[index].weight = data[i].weight;
        }
        //  Start building the Huffman tree
        int begin = data.length + 1;
        int end = 2 * data.length;
        for (int i = begin; i < end; i++) {
            //  Select the two subtrees with the smallest weight , Building the Huffman tree
            int[] min = select2HTNode(nodes, i);
            //  A merger of two
            nodes[i] = new TreeNode();
            nodes[i].lchild = min[0];
            nodes[i].rchild = min[1];
            nodes[i].weight = nodes[min[0]].weight + nodes[min[1]].weight;
            //  Set parent node index
            nodes[min[0]].parent = i;
            nodes[min[1]].parent = i;
        }
        return nodes;
    }
}