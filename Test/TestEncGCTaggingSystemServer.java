// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import jargs.gnu.CmdLineParser;

import Program.*;

class TestEncGCTaggingSystemServer {
    private static void printUsage() {
    	System.out.println("Usage: java TestTaggingSystemServer [{-d, --dir} databaseDirName]");
    }

    private static void process_cmdline_args(String[] args) {
    	CmdLineParser parser = new CmdLineParser();
    	CmdLineParser.Option optiondatabaseDirName = parser.addStringOption('d', "dir");

		try {
	    	parser.parse(args);
		}
		catch (CmdLineParser.OptionException e) {
	    	System.err.println(e.getMessage());
	    	printUsage();
	    	System.exit(2);
		}
		
		//ProgServer.databaseDirName = (String) parser.getOptionValue(optiondatabaseDirName, new String("database"));
		ProgServer.databaseDirName = (String) parser.getOptionValue(optiondatabaseDirName, new String("database_s"));
    }

    public static void main(String[] args) throws Exception {
    	//StopWatch.pointTimeStamp("Starting program");
    	process_cmdline_args(args);
	
    	EncGCTaggingSystemServer encgctaggingserver = new EncGCTaggingSystemServer();
    	encgctaggingserver.run();
    }
}