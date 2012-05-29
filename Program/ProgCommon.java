// Copyright (C) 2010 by Yan Huang <yh8h@virginia.edu>

package Program;

import java.io.*;

import Utils.Mode;

public abstract class ProgCommon {
    public static ObjectOutputStream oos        = null;              // socket output stream
    public static ObjectInputStream  ois        = null;              // socket input stream
    public Mode mMode;
}