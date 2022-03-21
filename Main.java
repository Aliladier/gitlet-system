package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.io.*;
import java.util.*;

public class Main {
    private static String command;
    private static int commandlen;
    private static Repository repository;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    private static final File STAGING_AREA = Utils.join(GITLET_DIR, "stage");
    private static final File COMMITS = Utils.join(GITLET_DIR,"commits");
    private static final File VERSIONS = Utils.join(GITLET_DIR,"versions");

    public static void init(){
        if(commandlen == 1){
            File gitlet = new File(".gitlet");
            if(gitlet.exists()){
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }
            else{
                repository = Repository.init();
            }
        }
        else{
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

        save();
    }

    public static  void add(String filename){
        if (commandlen != 2){
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        else{
            repository.add(filename);
        }

        save();
    }

    public static void find(String message){
        if (commandlen == 2){
            repository.find(message);
        } else{
            System.out.println("Incorrect operands!");
        }
    }

    public static void commit(String message){
        if(commandlen == 2){
            repository.commit(message);
        }
        else{
            System.out.println("Incorrect operands.");
        }

        save();
    }

    public static void log(){
        if (commandlen == 1){
            repository.log();
        }
        else{
            System.out.println("Incorrect operands.");
        }
    }

    public static void checkout(String filename){
        if(commandlen == 3){
            repository.checkout(filename);
        }
        else{
            System.out.println("Incorrect operands!");
        }
        save();
    }

    public static void checkout(String commitid , String filename){
        if (commandlen != 4){
            System.out.println("Incorrect operands!");
        }
        else{
            repository.checkout(commitid,filename);
        }

        save();
    }

    public static void rm(String filename){
        if (commandlen == 2){
            repository.rm(filename);
        }
        else{
            System.out.println("Incorrect operands!");
        }

        save();
    }

    public static void checkoutBranch(String branchName){
        if(commandlen == 2){
            repository.checkoutBranch(branchName);
        }
        else{
            System.out.println("Incorrect operands!");
        }

        save();
    }

    public static void status(){
        if (commandlen == 1){
            repository.status();
        }
        else{
            System.out.println("Incorrect operands!");
        }
    }

    public static void globallog(){
        if (commandlen == 1){
            repository.globallog();
        }
        else{
            System.out.println("Incorrect operands.");
        }
    }

    public static void branch(String branchName){
        if (commandlen == 2){
            repository.branch(branchName);
        }
        else{
            System.out.println("Incorrect operands.");
        }

        save();
    }

    public static void rmbranch(String branchName){
        if(commandlen == 2){
            repository.rmbranch(branchName);
        }
        else {
            System.out.println("Incorrect operands.");
        }

        save();
    }

    public static void save(){
        File outFile = Utils.join(GITLET_DIR,"Repo.txt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(repository);
            out.close();
        } catch (IOException excp) {
            System.exit(-1);
        }
    }

    public static void load(){
        File load = GITLET_DIR;
        if (load.exists()){
            File inFile = Utils.join(GITLET_DIR, "Repo.txt");
            try {
                ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
                repository = (Repository) inp.readObject();
                inp.close();
            } catch (IOException | ClassNotFoundException excp) {
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0){
            System.out.println("Please enter a command.");
            return;
        }
        command = args[0];
        commandlen = args.length;

        load();

        switch(command) {
            case "init":
                // TODO: handle the `init` command
                init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                commit(args[1]);
                break;
            case "log":
                log();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")){
                    checkout(args[2]);
                }
                else if (args.length == 4 && args[2].equals("--")) {
                    checkout(args[1], args[3]);
                }
                else if (args.length == 2){
                    checkoutBranch(args[1]);
                }
                else{
                    System.out.println("Incorrect operands.");
                }
                break;
            case "rm":
                rm(args[1]);
                break;
            case "status":
                status();
                break;
            case "find":
                find(args[1]);
                break;
            case "global-log":
                globallog();
                break;
            case "branch":
                branch(args[1]);
                break;
            case "rm-branch":
                rmbranch(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }
}
