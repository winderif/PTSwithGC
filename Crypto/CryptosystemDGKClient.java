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

import Utils.Create;

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
public class CryptosystemDGKClient extends CryptosystemAbstract {
	
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

    /**
     * Private key (p, q, vp, vq)
     */
    private BigInteger p;          // private key
    private BigInteger q;          // private key
    private BigInteger vp;         // private key
    private BigInteger vq;         // private key       
        
    private BigInteger vpvq;
    public BigInteger n_square;
        
    private HashMap<BigInteger, Integer> dechash;
    private boolean decrypttableIsValid = false;
    
    private static final BigInteger TWO = new BigInteger("2");
    
    public CryptosystemDGKClient() {
    	this.k = 1024;
    	this.t = 160;
    	this.l = 16;
        KeyGeneration(512, 64, null);
    }
    
    public CryptosystemDGKClient(int bitLengthVal, int certainty) {
    	this.k = 1024;
    	this.t = 160;
    	this.l = 16;
        KeyGeneration(bitLengthVal, certainty, null);
    }
    
    public CryptosystemDGKClient(int bitLengthVal, int certainty,
    		int bitSizeK, int bitSizeT, int limitL) {
    	this.k = bitSizeK;
    	this.t = bitSizeT;
    	this.l = limitL;
        KeyGeneration(bitLengthVal, certainty, null);
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
        // generate u the minimal prime number greater than l+2
        u = BigInteger.valueOf((1<<l) + 2).nextProbablePrime();        
        
        // generate vp, vq as a random t bit prime number
        /**
        vp = BigInteger.probablePrime(t, new Random());
        vq = BigInteger.probablePrime(t, new Random());
        */
        vp = new BigInteger(t, new Random());
        vq = new BigInteger(t, new Random());
        
        // store the product vp*vq
        vpvq = vp.multiply(vq);

        // DGK style to generate p and q from u and v:

        // p is chosen as rp * u * vp + 1 where r_p is randomly chosen such that p has roughly k/2 bits
        BigInteger rp; 
        BigInteger rq;
        
        BigInteger tmp = u.multiply(vp);        
        int needed_bits = 
        	k / 2 - (int)Math.ceil(Math.log(tmp.doubleValue()) / Math.log(2));        
        
        do
        {
        	rp = RandomIntMSBSet(needed_bits - 1).multiply(TWO);        	
            p = (rp.multiply(tmp)).add(BigInteger.ONE);            
        } while (!p.isProbablePrime(certainty));
        
        tmp = u.multiply(vq);
        needed_bits = 
        	k / 2 - (int)Math.ceil(Math.log(tmp.doubleValue()) / Math.log(2));
        
        do
        {
            rq = RandomIntMSBSet(needed_bits - 1).multiply(TWO);
            q = (rq.multiply(tmp)).add(BigInteger.ONE);
        } while (!q.isProbablePrime(certainty));

        // RSA modulus n
        n = p.multiply(q);
        n_square = n.multiply(n);
        
        /**
        h must be random in Zn* and have order vp*vq. We
        choose it by setting

        h = h' ^{rp * rq * u}.

        Seeing h as (hp, hq) and h' as (h'p, h'q) in Zp* x Zq*, we
        then have

        (hp^vpvq, hq^vpvq) = (h'p^{rp*u*vp}^(rq*vq), h'q^{rq*u*vq}^(rp*vp))
        = (1^(rq*vq), 1^(rp*vp)) = (1, 1)

        which means that h^(vpvq) = 1 in Zn*.

        So we only need to check that h is not 1 and that it really
        is in Zn*.
        */        
        BigInteger r;
        
        tmp = rp.multiply(rq).multiply(u);
        
        while (true) {                	
            r = RandomIntLimit(n);
            h = r.modPow(tmp, n);
            if(h.equals(BigInteger.ONE)) { 
            	continue;
            }
            if(h.gcd(n).equals(BigInteger.ONE)) { 
            	break;
            }            
        }
        
        /**
        g is chosen at random in Zn* such that it has order uv. This
        is done in much the same way as for h, but the order of
        power of the random number might be u, v or uv. We therefore
        also check that g^u and g^v are different from 1.
        */

       BigInteger rprq = rp.multiply(rq);

       while (true) {    	   
    	   r = RandomIntLimit(n);
           g = r.modPow(rprq, n);

           // test if g is "good":
           if (g.equals(BigInteger.ONE)) continue;
           if (g.gcd(n).equals(BigInteger.ONE) == false) { 
        	   continue;
           }
           if (g.modPow(u, n).equals(BigInteger.ONE)) continue;      
           // test if ord(g) == u
           if (g.modPow(vp, n).equals(BigInteger.ONE)) continue;     
           // test if ord(g) == vp
           if (g.modPow(vq, n).equals(BigInteger.ONE)) continue;     
           // test if ord(g) == vq
           if (g.modPow(u.multiply(vp), n).equals(BigInteger.ONE)) continue; 
           // test if ord(g) == u*vp
           if (g.modPow(u.multiply(vq), n).equals(BigInteger.ONE)) continue; 
           // test if ord(g) == u*vq
           if (g.modPow(vpvq, n).equals(BigInteger.ONE)) continue;   
           // test if ord(g) == vp*vq
           break;  // g has passed all tests
       }            
       BigInteger gv = g.modPow(vp, p);
       
       decrypttableIsValid = true;
       
       dechash = Create.hashMap();
       for (int i = 0; i < u.intValue(); i++) {
    	   BigInteger index = 
    		   gv.modPow(BigInteger.valueOf(i), p).mod(BigInteger.ONE.shiftLeft(48));
    	   dechash.put(index, i);
       }
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
    	BigInteger message = c.modPow(vp, p);
    	
    	if (decrypttableIsValid == true) {
    		message = message.mod(BigInteger.ONE.shiftLeft(48));
            if (dechash.containsKey(message)) {            
                Object x = dechash.get(message);
                BigInteger res = new BigInteger(x.toString());
                return res;
            }
    	}
    	else {
    		BigInteger gv = g.modPow(vp, p);    
        	
        	for(int m = 0; m < u.intValue(); m++) {
        		BigInteger m_Big = BigInteger.valueOf(m);
                if (message.equals(gv.modPow(m_Big, p))) { 
                	return m_Big;
                }            
        	}	
    	}    	
        // should not be reached    	
    	System.out.println("This value is no regular ciphertext");
    	return BigInteger.ZERO;
    }
    
    /**
     * To get pair of public key
     * @return pair of public key as a BigInteger[]
     * 12.03.13 winderif 
     */
    public BigInteger[] getPublicKey() {
    	BigInteger[] tmp = {n, g, h, u};
    	return tmp;
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
    
    /**
     * Returns a random integer with 'bits' bits and the MSB set.
     * @param bits
     * @return
     */
    private BigInteger RandomIntMSBSet(int bits) {    	
    	return SetBit( RandomIntBits(bits - 1), bits - 1 );     	   
    }
    
    /**
     * Returns a random integer less than 2^bits
     * @param bits
     * @return
     */
    private final BigInteger RandomIntBits(int bits) {
    	if (bits < 0) { 
    		throw new ArithmeticException("Enter a positive bitcount");
    	}    	
    	return RandomIntLimit((BigInteger.ONE).shiftLeft(bits));
    }
    
    private final BigInteger SetBit(BigInteger b, int i) {    	
    	if( i>=0 ) { 
    		b = b.or((BigInteger.ONE).shiftLeft(i));
    	}
    	return b;
    }       
    
    public static void main(String[] args) {
    	CryptosystemDGKClient dgc_c = 
    		new CryptosystemDGKClient();
    	BigInteger a = new BigInteger("10");
    	BigInteger b = new BigInteger("20");
    	BigInteger ea = dgc_c.Encryption(a);
    	BigInteger eb = dgc_c.Encryption(b);
    	BigInteger ec = ea.multiply(eb).mod(dgc_c.n);
    	BigInteger ed = ea.modPow(b, dgc_c.n);
    	BigInteger c = dgc_c.Decryption(ec);
    	BigInteger d = dgc_c.Decryption(ed);
    	System.out.println("a\t" + a);
    	System.out.println("b\t" + b);
    	System.out.println("c\t" + c);
    	System.out.println("d\t" + d);
    	double s = System.nanoTime();
    	for(int i=0; i<1000; i++) {
    		ea = dgc_c.Encryption(a);
    	}
    	double e = System.nanoTime();
    	double time = (e - s) / 1000000000.0; 
    	System.out.println("time\t" + time);
    }
}
