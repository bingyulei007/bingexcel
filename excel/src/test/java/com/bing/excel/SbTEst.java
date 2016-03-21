package com.bing.excel;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SbTEst {
	@Test
public void testP(){
		Map<Integer, String> indexToNameMap = Maps.newHashMap();
		indexToNameMap.put(0, "nimei");
		indexToNameMap.put(0, "nimei2");
		indexToNameMap.put(1, "nimei3");
		System.out.println(indexToNameMap.get(2));
		
}
}
