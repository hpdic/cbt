package com.nvada.blocklite.utils;

import java.util.ArrayList;
import java.util.List;

import com.nvada.blocklite.Block;

public class TreeNode<T> {
	/**
	 * Node has: 
	 * 	1. Parent 
	 * 	2. List of children
	 *  3. data	- here it will be Block
	 * */
	public TreeNode<T> parent = null;
	public List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();
    public T data = null;

    public TreeNode(T data) {
        this.data = data;
        this.parent = null;
    }

    public TreeNode(T data, TreeNode<T> parent) {
        this.data = data;
        this.parent = parent;
        parent.children.add(this);
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public void setParent(TreeNode<T> parent) {
        parent.addChild(this.getData());
        this.parent = parent;
    }

    public void addChild(T data) {
        TreeNode<T> child = new TreeNode<T>(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        if(this.children.size() == 0) 
            return true;
        else 
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }
    
    /**
     * Find the node in the subtree rooted at node that contains the data d
     * */
    public TreeNode<T> findNode(T d){
    	if(this.data.equals(d)){
    		return this;
    	}
    	TreeNode<T> tmp=null;
    	for(int i=0;i<this.children.size();i++){
    		tmp = children.get(i).findNode(d);
    		if(tmp!=null){
    			return tmp;
    		}
    	}
    	return null;
    }

    /**
     * Find the node in the subtree rooted at node that contains the data d
     * */
    public TreeNode<T> findNodeByBlockID(String id) {
    	if(((Block)this.data).matchBlockID(id)){
    		return this;
    	}
    	
    	TreeNode<T> tmp=null;
    	for(int i=0;i<this.children.size();i++){
    		tmp = children.get(i).findNodeByBlockID(id);
    		if(tmp!=null){
    			return tmp;
    		}
    	}
    	return null;
    }


    public void printNode(String ident){
    	System.out.println(ident+"********************************************");
    	((Block) this.data).printBlock(ident);
    	System.out.println(ident+"Parent Node data:");
    	((Block) this.parent.data).printBlock(ident);
    	System.out.println(ident+"Children Nodes data:");
    	for(int i=0;i<this.children.size();i++){
    		System.out.println(ident+"Child #"+(i+1));
    		((Block) this.children.get(i).data).printBlock(ident);
    		System.out.println();
    	}
    	System.out.println(ident+"********************************************");
    }
    
    public void printOnlyNode(String ident){
    	System.out.println(ident+"********************************************");
    	((Block) this.data).printBlock(ident);
    	System.out.println(ident+"********************************************");
    }
    
    public void printSubTree(String ident){
    	this.printOnlyNode(ident);
    	for(int i=0;i<this.children.size();i++){
    		this.children.get(i).printSubTree(ident+"\t");
    	}
    }
    
}