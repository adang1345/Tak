import java.lang.reflect.InvocationTargetException;

/** Sandbox for testing code snippets. */
public class Sandbox {
	
	public static abstract class A {
		protected int i;
		
		protected A(int i) {
			this.i = i;
		}
		
		public A clone() {
			Class<? extends A> subclass = this.getClass();
			try {
				return subclass.getDeclaredConstructor(int.class).newInstance(i);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public abstract int getI();
	}
	
	public static class B extends A {
		public B(int i) {
			super(i);
		}
		
		public int getI() {
			return i;
		}
	}
	
	public static void main(String[] args) {
		B b = new B(1);
		A a = b.clone();
		System.out.println(b == a);
		System.out.println(b.getI() == a.getI());
	}
	
}
