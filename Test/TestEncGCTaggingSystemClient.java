// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import java.util.*;
import java.math.*;

import jargs.gnu.CmdLineParser;

import Utils.*;
import Program.*;

class TestEncGCTaggingSystemClient {

    private static void printUsage() {
    	System.out.println("Usage: java TestTaggingSystemClient [{-s, --server} servername] [{-d, --dir} queryDirName]");
    }

    private static void process_cmdline_args(String[] args) {
    	CmdLineParser parser = new CmdLineParser();
    	CmdLineParser.Option optionServerIPname = parser.addStringOption('s', "server");	
    	CmdLineParser.Option optionQueryDirName = parser.addStringOption('d', "dir");

		try {
	    	parser.parse(args);
		}
		catch (CmdLineParser.OptionException e) {
	    	System.err.println(e.getMessage());
	    	printUsage();
	    	System.exit(2);
		}		
		ProgClient.serverIPname = (String) parser.getOptionValue(optionServerIPname, new String("localhost"));	
		//ProgClient.queryDirName = (String) parser.getOptionValue(optionQueryDirName, new String("query"));
		ProgClient.queryDirName = (String) parser.getOptionValue(optionQueryDirName, new String("query_s"));
    }

    public static void main(String[] args) throws Exception {
	//	StopWatch.pointTimeStamp("Starting program");
    	process_cmdline_args(args);
	
    	EncGCTaggingSystemClient encgctaggingclient = new EncGCTaggingSystemClient();
    	encgctaggingclient.run();
    }
}