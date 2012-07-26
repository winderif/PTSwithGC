package Crypto;
/**
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.math.*;
import java.util.*;
import Crypto.CryptosystemAbstract;

/**
 * Paillier Cryptosystem <br><br>
 * References: <br>
 * [1] Pascal Paillier, "Public-Key Cryptosystems Based on Composite Degree Residuosity Classes," EUROCRYPT'99.
 *    URL: <a href="http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf">http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf</a><br>
 * 
 * [2] Paillier cryptosystem from Wikipedia. 
 *    URL: <a href="http://en.wikipedia.org/wiki/Paillier_cryptosystem">http://en.wikipedia.org/wiki/Paillier_cryptosystem</a>
 * @author Kun Liu (kunliu1@cs.umbc.edu)
 * @version 1.0
 */
public class CryptosystemPaillierClient extends CryptosystemAbstract {

    /**
     * p and q are two large primes. 
     * lambda = lcm(p-1, q-1) = (p-1)*(q-1)/gcd(p-1, q-1).
     */
    private BigInteger p,  q,  lambda;
    /**
     * n = p*q, where p and q are two large primes.
     */
    public BigInteger n;
    /**
     * nsquare = n*n
     */
    public BigInteger nsquare;
    /**
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
     */
    private BigInteger g;

    /**
     * Constructs an instance of the Paillier cryptosystem.
     * @param bitLengthVal number of bits of modulus
     * @param certainty The probability that the new BigInteger represents a prime number will exceed (1 - 2^(-certainty)). The execution time of this constructor is proportional to the value of this parameter.
     */

    /**
     * Constructs an instance of the Paillier cryptosystem with 512 bits of modulus and at least 1-2^(-64) certainty of primes generation.
     */
    public CryptosystemPaillierClient() {
        KeyGeneration(512, 64, null);
    }
    
    public CryptosystemPaillierClient(int bitLengthVal, int certainty) {
        KeyGeneration(bitLengthVal, certainty, null);
    }

    /**
     * Sets up the public key and private key.
     * @param bitLengthVal number of bits of modulus.
     * @param certainty The probability that the new BigInteger represents a prime number will exceed (1 - 2^(-certainty)). The execution time of this constructor is proportional to the value of this parameter.
     */
    public void KeyGeneration(int bitLengthVal, int certainty, BigInteger[] publicKey) {
        bitLength = bitLengthVal;
        /*Constructs two randomly generated positive BigIntegers that are probably prime, with the specified bitLength and certainty.*/
        p = new BigInteger(bitLength / 2, certainty, new Random());
        q = new BigInteger(bitLength / 2, certainty, new Random());

        n = p.multiply(q);
        nsquare = n.multiply(n);

        //g = new BigInteger("2");
        g = n.add(BigInteger.ONE);
        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)).divide(
                p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        /* check whether g is good.*/
        if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() != 1) {
            System.out.println("g is not good. Choose g again.");
            System.exit(1);
        }
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function explicitly requires random input r to help with encryption.
     * @param m plaintext as a BigInteger
     * @param r random plaintext to help with encryption
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m, BigInteger r) {
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function automatically generates random input r (to help with encryption).
     * @param m plaintext as a BigInteger
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m) {    	
        BigInteger r = new BigInteger(bitLength, new Random());        
        return (m.equals(BigInteger.ZERO))?
        		(this.EncZero):
        		(m.multiply(n).add(BigInteger.ONE)).multiply(r.modPow(n, nsquare)).mod(nsquare);
        		//(g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare));
    }

    /**
     * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where u = (L(g^lambda mod n^2))^(-1) mod n.
     * @param c ciphertext as a BigInteger
     * @return plaintext as a BigInteger
     */
    public BigInteger Decryption(BigInteger c) {
        BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
        return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
    }
    
    /**
     * To get pair of public key
     * @return pair of public key as a BigInteger[]
     * 12.03.13 winderif 
     */
    public BigInteger[] getPublicKey() {
    	BigInteger[] tmp = {p, q};
    	return tmp;
    }
    
    public static void main(String[] args) {
    	CryptosystemPaillierClient p_c = 
    		new CryptosystemPaillierClient(512, 64);
    	
    	int num = 20;
    	int bitR = 10;
    	int bitW = 10; 
    	BigInteger[] w = new BigInteger[num];
    	BigInteger[] wEnc = new BigInteger[num];
    	BigInteger[] r = new BigInteger[num];
    	BigInteger MAX = BigInteger.ONE.shiftLeft(bitR-1);
    	BigInteger tmp = BigInteger.ZERO;
    	BigInteger tmpShift = BigInteger.ZERO;
    	BigInteger sumEnc = BigInteger.ONE;
    	for(int i=0; i<num; i++) {
    		w[i] = new BigInteger(Integer.valueOf(i+1).toString());
    		wEnc[i] = p_c.Encryption(w[i]);
    		sumEnc = 
    			sumEnc.multiply(wEnc[i].modPow(
    							BigInteger.ONE.shiftLeft((bitR + bitW) * i), 
    							p_c.nsquare));
    		    		
    		r[i] = new BigInteger(bitR + bitW, new Random());
    		BigInteger sum = w[i].add(MAX).add(r[i]);
    		BigInteger shift = MAX.add(r[i]);
    		tmp = tmp.add(sum.shiftLeft((bitR + bitW) * i));
    		tmpShift = tmpShift.add(shift.shiftLeft((bitR + bitW) * i));
    		System.out.print(w[i] + " " + r[i] + " ");
    	}
    	BigInteger tmpEnc = p_c.Encryption(tmpShift);
    	BigInteger xEnc = tmpEnc.multiply(sumEnc).mod(p_c.nsquare);
    	System.out.println();
    	System.out.println("tmp\t" + tmp);
    	System.out.println("xEnc\t" + p_c.Decryption(xEnc));
    	
    	BigInteger mask = BigInteger.ONE.shiftLeft(bitR + bitW).subtract(BigInteger.ONE);
    	BigInteger tmpW;
    	BigInteger tmpWpack;
    	BigInteger s3_p = BigInteger.ZERO;
    	BigInteger s3Pack = BigInteger.ZERO;
    	BigInteger xPack = p_c.Decryption(xEnc);
    	for(int i=0; i<num; i++) {    		
    		tmpWpack = xPack.and(mask).subtract(MAX);
    		tmpW = tmp.and(mask).subtract(MAX);    	
    		
    		xPack = xPack.shiftRight(bitR + bitW);
    		tmp = tmp.shiftRight(bitR + bitW);
    		
    		s3Pack = s3Pack.add(tmpWpack.multiply(tmpWpack));
    		s3_p = s3_p.add(tmpW.multiply(tmpW));
    		
    		System.out.print(tmpW + " " + tmpWpack + " ");
    	}
    	System.out.println();
    	BigInteger s3pEnc = p_c.Encryption(s3Pack);
    	
    	BigInteger TWO = BigInteger.ONE.add(BigInteger.ONE);
    	BigInteger r_sum = BigInteger.ZERO;
    	BigInteger wr_sum = BigInteger.ZERO;
    	BigInteger wr_sumEnc = BigInteger.ONE;
    	
    	for(int i=0; i<num; i++) {
    		r_sum = r_sum.add(r[i].multiply(r[i]));
    		wr_sum = wr_sum.add(TWO.multiply(w[i]).multiply(r[i]));
    		wr_sumEnc = wr_sumEnc.multiply(wEnc[i].modPow(TWO.multiply(r[i]), p_c.nsquare));
    	}    	
    	wr_sumEnc = wr_sumEnc.modInverse(p_c.nsquare);    	
    	BigInteger r_sumEnc = p_c.Encryption(r_sum.negate());
    	
    	BigInteger s3 = s3_p.subtract(r_sum).subtract(wr_sum);
    	BigInteger s3Enc = s3pEnc.multiply(r_sumEnc).multiply(wr_sumEnc).mod(p_c.nsquare);
    	System.out.println("s3\t" + s3);
    	System.out.println("s3\t" + p_c.Decryption(s3Enc));
    	
    	BigInteger a = new BigInteger("10");
    	BigInteger b = new BigInteger("20");
    	BigInteger ea = p_c.Encryption(a);
    	BigInteger eb = p_c.Encryption(b);
    	BigInteger ec = ea.multiply(eb).mod(p_c.nsquare);
    	BigInteger ed = ea.modPow(b, p_c.nsquare);
    	BigInteger c = p_c.Decryption(ec);
    	BigInteger d = p_c.Decryption(ed);
    	System.out.println("a\t" + a);
    	System.out.println("b\t" + b);
    	System.out.println("c\t" + c);
    	System.out.println("d\t" + d);    	
    }
}
