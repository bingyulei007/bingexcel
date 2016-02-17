package com.bing.excel.reader.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * listrow 对象
 * @author shizhongtao
 *
 * @date 2016-2-17
 * Description:  
 */
public class ListRow implements Iterable<CellKV>{
	private List<CellKV> list = null;
	private int minIndex=-1;
	private int maxIndex=-1;
	
	/**
	 * 会创建一个新的arraylist 对象。
	 * @return 返回新的list对象
	 */
	public List<CellKV> getList() {
		if (list == null)
			return Collections.emptyList();
		return new ArrayList<>(list);
	}
	public  ListRow add(CellKV kv){
		if(list==null){
			list=new ArrayList<>();
		}
		list.add(kv);
		int index=kv.getIndex();
		if(index<0){
			throw new IllegalArgumentException("CellKV 中index不合法");
		}
		if(index>maxIndex){
			maxIndex=index;
		}
		if(minIndex==-1){
			minIndex=index;
		}else{
			if(index<minIndex){
				minIndex=index;
			}
		}
		return this;
	}
	public String toString(){
		if (list == null)
			return Collections.emptyList().toString();
		return list.toString();
	}
	public String[] toArray() {
		if(maxIndex!=-1){
			String[]  arr=new String[maxIndex+1];
			for (CellKV kv : list) {
				arr[kv.getIndex()]=kv.getValue();
			}
			
			return arr;
		}else{
			return new String[0];
		}
	}
	public String[] toArray(int length) {
		
			String[]  arr=new String[length];
			if(maxIndex!=-1){
				for (CellKV kv : list) {
					if(kv.getIndex()<length){
						arr[kv.getIndex()]=kv.getValue();
					}
				}
			}
			return arr;
		
	}
	public void clear() {
		if(list!=null){
		     list.clear();
		     
		}
	}
	@Override
	public Iterator<CellKV> iterator() {
		
		if(maxIndex==-1){
		   list=new ArrayList<CellKV>();
		}
		return list.iterator();
	}
	
	
}
