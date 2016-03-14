package com.bing.excel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.bing.excel.GuavalTEst.Person;
import com.bing.utils.ReflectDependencyFactory;

  
	  
public	 class FieldModifierSpy {  
	  volatile int share;  
	  int instance;  
	  
	  class Inner {  
	  }  
	  
	  public static void main(String... args) {  
	    try {  
	      Class<?> c = FieldModifierSpy.class;  
	      int searchMods = 0x0;  
	      for (int i = 1; i < args.length; i++) {  
	        searchMods |= modifierFromString(args[i]);  
	      }  
	  
	      Field[] flds = c.getDeclaredFields();  
	      System.out.format("Fields in Class '%s' containing modifiers:  %s%n", c  
	          .getName(), Modifier.toString(searchMods));  
	      boolean found = false;  
	      for (Field f : flds) {  
	        int foundMods = f.getModifiers();  
	        // Require all of the requested modifiers to be present  
	        if ((foundMods & searchMods) == searchMods) {  
	        	 System. out.format("%-8s [ synthetic=%-5b enum_constant=%-5b ]%n", f  
	              .getName(), f.isSynthetic(), f.isEnumConstant());  
	          found = true;  
	        }  
	      }  
	  
	      if (!found) {  
	    	  System.out.format("No matching fields%n");  
	      }  
	  
	      // production code should handle this exception more gracefully  
	    } catch (Exception x) {  
	      x.printStackTrace();  
	    }  
	  }  
	  
	  private static int modifierFromString(String s) {  
	    int m = 0x0;  
	    if ("public".equals(s))  
	      m |= Modifier.PUBLIC;  
	    else if ("protected".equals(s))  
	      m |= Modifier.PROTECTED;  
	    else if ("private".equals(s))  
	      m |= Modifier.PRIVATE;  
	    else if ("static".equals(s))  
	      m |= Modifier.STATIC;  
	    else if ("final".equals(s))  
	      m |= Modifier.FINAL;  
	    else if ("transient".equals(s))  
	      m |= Modifier.TRANSIENT;  
	    else if ("volatile".equals(s))  
	      m |= Modifier.VOLATILE;  
	    return m;  
	  }  
	}  
class FieldMo {

	@Test
	public void testContr(){
		Object newInstance = ReflectDependencyFactory.newInstance(Person.class, new Object[]{23,"0",12});
		System.out.println(newInstance);
	}
	
	public void testFinal(){
		Person  a=new Person(1, "zhizhang", 13);
		//dos(a);
		Field[] declaredFields = Person.class.getFields();
		System.out.println(Modifier.STATIC | Modifier.TRANSIENT);
		for (int i = 0; i < declaredFields.length; i++) {
			Class<?> clazz = declaredFields[i].getDeclaringClass();
			System.out.println(declaredFields[i].getName());
			
			System.out.print (" "+(declaredFields[i].getModifiers()&((Modifier.STATIC | Modifier.TRANSIENT))));
			System.out.println (" "+declaredFields[i].isSynthetic());
		}
		//System.out.println(a.getName()+":"+a.getId());
	}
	
	public void dos(final Person p){
		//p=new Person("nihao", 12);
		//p.setId(5);
	}
}
enum Spy {  
	  BLACK, WHITE  
	} 