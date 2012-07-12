package Utils;
import java.util.Map;
import java.util.Iterator;
import Crypto.CryptosystemPaillierServer;
import java.math.BigInteger;

public class Print {
	public static void printMap(Map tmpMap, String info) {
		System.out.println(info);
		Iterator iter = tmpMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, Double> p = (Map.Entry)iter.next();
			System.out.println(p.getKey() + "\t" + p.getValue());
		}
	}
	
	public static void printEncMap(Map tmpMap, String info, CryptosystemPaillierServer paillier) {
		System.out.println(info);
		Iterator iter = tmpMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, BigInteger> p = (Map.Entry)iter.next();
			System.out.println(p.getKey() + "\t" + paillier.Decryption(p.getValue()));
		}
	}
	
	public static void printArray(BigInteger[] tmpArray, String info) {
		System.out.println(info);
		for(int i=0; i<tmpArray.length; i++) {
			System.out.print(tmpArray[i]);
		}
		System.out.println();
	}
	
	public static void printEncArray(BigInteger[] tmpArray, String info, CryptosystemPaillierServer paillier) {
		System.out.println(info);
		for(int i=0; i<tmpArray.length; i++) {
			System.out.print(paillier.Decryption(tmpArray[i]));
		}
		System.out.println();
	}
}
