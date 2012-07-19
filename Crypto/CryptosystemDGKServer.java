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
 * DGK cryptosystem
 * 12.07.19
 * Reference"
 * [1] CrypTool 2.0: https://www.cryptool.org/trac/CrypTool2/wiki
 * 
 * @author winderif
 * @version 1.0.0
 */
public class CryptosystemDGKServer extends CryptosystemAbstract {
	
	private int k;
    private int t;
    private int l;
    /**
     * Public key (n, g, h, u)
     */
	public BigInteger n;        // public key
    private BigInteger g;          // public key
    private BigInteger h;          // public key
    private BigInteger u;          // public key
    
    public BigInteger n_square;
    
    public CryptosystemDGKServer() {
    	this.k = 1024;
    	this.t = 160;
    	this.l = 16;        
    }
    
    public CryptosystemDGKServer(BigInteger[] publicKey) {
    	this.k = 1024;
    	this.t = 160;
    	this.l = 16;
        KeyGeneration(512, 64, publicKey);
    }        

    /**
     * Sets up the public key and private key.
     * @param bitLengthVal number of bits of modulus.
     * @param certainty The probability that the new BigInteger represents a prime number will exceed (1 - 2^(-certainty)). The execution time of this constructor is proportional to the value of this parameter.
     */
    public void KeyGeneration(int bitLengthVal, int certainty, BigInteger[] publicKey) {
    	if (l < 8 || l>16) {
            System.out.println("Choose parameter l from the interval 8<=l<=16 !");
            System.exit(1);
    	}
        if (t <= l) {
            System.out.println("Parameter t must be greater than l.");
            System.exit(1);
        }
        if (k <= t) {
            System.out.println("Parameter k must be greater than t.");
            System.exit(1);
        }
        if (k % 2 != 0) {
            System.out.println("Parameter k has to be an even number!");
            System.exit(1);
        }       
        if (k / 2 < l + t + 10) {
            System.out.println("Choose parameters k,l,t so that k/2 >= l+t+10 !");
            System.exit(1);
        }              

        // RSA modulus n
        n = publicKey[0];
        n_square = n.multiply(n);
        
        g = publicKey[1];
        h = publicKey[2];        
        
        // generate u the minimal prime number greater than l+2
        u = publicKey[3];                
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function explicitly requires random input r to help with encryption.
     * @param m plaintext as a BigInteger
     * @param r random plaintext to help with encryption
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m, BigInteger r) {
    	if(m.compareTo(this.u) == -1) {        	        	       
            return (g.modPow(m, n).multiply(h.modPow(r, n))).mod(n);
        }
        // If m >= u, then result may be error.
        else {
        	throw new ArithmeticException("Message is bigger than U - this will produce a wrong result!");
        } 
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function automatically generates random input r (to help with encryption).
     * @param m plaintext as a BigInteger
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m) {    	
        if(m.compareTo(this.u) == -1) {        	
        	BigInteger r = RandomIntLimit(n_square);
        	        
            return (g.modPow(m, n).multiply(h.modPow(r, n))).mod(n);            
        }
        // If m >= u, then result may be error.
        else {
        	throw new ArithmeticException("Message is bigger than U - this will produce a wrong result!");
        }
    }

    /**
     * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where u = (L(g^lambda mod n^2))^(-1) mod n.
     * @param c ciphertext as a BigInteger
     * @return plaintext as a BigInteger
     */
    public BigInteger Decryption(BigInteger c) {    	       
    	System.err.println("\t[S]\tCryptosystem server cannot use the method of decryption.");
    	return c;
    }
    
    /**
     * Returns a random integer less than limit
     * @param limit
     * @return
     */
    private BigInteger RandomIntLimit(BigInteger limit) {
    	Random rnd = new Random();    	
    	return new BigInteger(limit.bitLength(), rnd);
    }      
    
    public static void main(String[] args) {
    	CryptosystemDGKClient dgc_c = 
    		new CryptosystemDGKClient();
    	CryptosystemDGKServer dgc_s = 
    		new CryptosystemDGKServer(dgc_c.getPublicKey());
    	BigInteger a = new BigInteger("10");
    	BigInteger b = new BigInteger("20");
    	BigInteger ea = dgc_s.Encryption(a);
    	BigInteger eb = dgc_s.Encryption(b);
    	BigInteger ec = ea.multiply(eb).mod(dgc_s.n);
    	BigInteger ed = ea.modPow(b, dgc_s.n);
    	BigInteger c = dgc_c.Decryption(ec);
    	BigInteger d = dgc_c.Decryption(ed);
    	System.out.println("a\t" + a);
    	System.out.println("b\t" + b);
    	System.out.println("c\t" + c);
    	System.out.println("d\t" + d);
    }
}
