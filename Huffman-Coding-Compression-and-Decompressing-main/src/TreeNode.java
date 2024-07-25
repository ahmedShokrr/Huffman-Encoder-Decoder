import java.io.Serializable;

class TreeNode implements Serializable {
    //  Symbols to be coded （ Leaf nodes use ）
    public byte symbol;
    //  The weight of the node
    public int weight;
    //  Parent node
    public int parent;
    //  Left child node
    public int lchild;
    //  Right child node
    public int rchild;
}

