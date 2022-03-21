package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import java.io.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    private static final File STAGING_AREA = Utils.join(GITLET_DIR, "stage");
    private static final File COMMITS = Utils.join(GITLET_DIR,"commits");
    private static final File VERSIONS = Utils.join(GITLET_DIR,"versions");

    private String currentBranch;
    private String head;
    private List<String> removed;
    private HashMap<String, String> branches;
    private HashMap<String, Commit> things;


    /* TODO: fill in the rest of this class. */

    public Repository(){
        this.currentBranch = "master";
        this.branches = new HashMap<>();
        this.removed = new ArrayList<>();

        this.things = new HashMap<>();
        Commit initial = new Commit("initial commit", "", new HashMap<>());
        things.put(initial.getId(), initial);
        this.head = initial.getId();

        this.branches.put(currentBranch,head);
    }

    public Repository(String currentBranch, String head, List<String> removed, HashMap<String, String > branches, HashMap<String ,Commit> things){
        this.currentBranch = currentBranch;
        this.head = head;
        this.removed = removed;
        this.branches = branches;
        this.things = things;
    }

    public static Repository init(){
        File gitlet = GITLET_DIR;
        gitlet.mkdir();
        File others = STAGING_AREA;
        others.mkdir();
        others = COMMITS;
        others.mkdir();
        others = VERSIONS;
        others.mkdir();

        return new Repository();
    }

    static void copyFile(File srcPathStr, File desPathStr) {
        try{
            FileInputStream fis = new FileInputStream(srcPathStr);
            FileOutputStream fos = new FileOutputStream(desPathStr);
            byte datas[] = new byte[1024*8];
            int len = 0;//创建长度
            while((len = fis.read(datas))!=-1) {
                fos.write(datas,0,len);
            }
            fis.close();//释放资源
            fos.close();//释放资源
        } catch (Exception e) {
            System.out.println("Copy failed");
            e.printStackTrace();
        }
    }

    public void add(String filename){
        File toBeAdded = new File(filename);
        if (!toBeAdded.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        else{
            byte[] content = Utils.readContents(toBeAdded);
            String hash = Utils.sha1(content);
            Commit current = things.get(head);
            if(current.getContents().get(filename) != null && current.getContents().get(filename).equals(hash)){
                if(removed.contains(filename)){
                    removed.remove(filename);
                }
                return;
            }
            if(removed.contains(filename)){
                removed.remove(filename);
            }
            File added = Utils.join(STAGING_AREA,filename);
            Utils.writeContents(added,"");
            copyFile(toBeAdded, added);
        }
    }

    public void log() {
        Commit current = things.get(head);
        while(current != null){
            System.out.println(current);
            String parentId = current.getParent();
            current = things.get(parentId);
        }
        System.out.println("");
    }

    public void commit(String message){
        if (message.equals("")){
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        File stage = STAGING_AREA;
        File[] stagedFiles = stage.listFiles();
        if (stagedFiles.length == 0 && removed.size() == 0){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit current = things.get(head);
        HashMap<String ,String> contents = new HashMap<>(current.getContents());

        for (String removal : removed){
            contents.remove(removal);
        }


        for (File stagedFile: stagedFiles){
            byte[] bytes = Utils.readContents(stagedFile);
            String hash = Utils.sha1(bytes);

            String filename = stagedFile.getName();
            contents.put(filename, hash);

            filename = stagedFile.getName() + hash;
            File toVersion = Utils.join(VERSIONS,filename);
            Utils.writeContents(toVersion, "");
            copyFile(stagedFile,toVersion);
            stagedFile.delete();
        }


        Commit now = new Commit(message, current.getId(), contents);
        head = now.getId();
        things.put(head, now);
        branches.put(currentBranch, head);
        removed.clear();
    }

    public void checkout(String filename){
        Commit nowHead = things.get(head);

        if(!nowHead.getContents().containsKey(filename)){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String hash = nowHead.getContents().get(filename);

        File backToMain = Utils.join(CWD,filename);
        Utils.writeContents(backToMain,"");

        File thisVersion = Utils.join(VERSIONS,filename + hash);
        copyFile(thisVersion, backToMain);
    }

    public void checkout(String commitId, String filename){
        if (!things.containsKey(commitId)){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        else if (!things.get(commitId).getContents().containsKey(filename)){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        Commit checkoutCommit = things.get(commitId);
        String hash = checkoutCommit.getContents().get(filename);
        File backToMain = Utils.join(CWD,filename);
        Utils.writeContents(backToMain,"");

        File thisVersion = Utils.join(VERSIONS,filename + hash);
        copyFile(thisVersion, backToMain);
    }

    public void rm(String filename){
        boolean isDeleted = false;
        boolean isStaged = false;
        boolean isTracked = false;

        HashMap<String, String> contents = things.get(head).getContents();
        if (contents.containsKey(filename)){
            isTracked = true;
        }

        if(isTracked){
            File removedFile = Utils.join(CWD,filename);
            isDeleted = true;
            removedFile.delete();
            removed.add(filename);
        }

        File Staged = Utils.join(STAGING_AREA, filename);
        isStaged = Staged.exists();

        if(isStaged){
            Staged.delete();
        }

        if (isTracked == false && isStaged == false){
            System.out.println("No reason to remove the file.");
        }
    }

    public void checkoutBranch(String branchName){
        if (branchName.equals(currentBranch)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        if(!branches.containsKey(branchName)){
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        Commit curr = things.get(head);
        HashMap<String,String> currContents = curr.getContents();
        String commitId = branches.get(branchName);
        Commit now = things.get(commitId);
        HashMap<String, String> nowContents = now.getContents();

        for (File currFile : CWD.listFiles()){
            if (currFile.isFile()) {
                byte[] content = Utils.readContents(currFile);
                String hash = Utils.sha1(content);
                if (nowContents.containsKey(currFile.getName()) && !currContents.containsKey(currFile.getName()) && !hash.equals(nowContents.get(currFile.getName()))) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        for (File currFile : CWD.listFiles()){
            if(currContents.containsKey(currFile) && !nowContents.containsKey(currFile)){
                currFile.delete();
            }
        }

        for (Map.Entry<String, String> entry : nowContents.entrySet()){
            String filename = entry.getKey();
            String hashCode = entry.getValue();
            File backToMain = Utils.join(CWD, filename);
            File Version = Utils.join(VERSIONS,filename + hashCode);

            Utils.writeContents(backToMain, "");
            copyFile(Version,backToMain);
        }

        head = commitId;
        currentBranch = branchName;
    }

    public void find(String message){
        int count = 0;
        for (Commit commit: things.values()){
            if (message.equals(commit.getMessage())){
                System.out.println(commit.getId());
                count += 1;
            }
        }
        if (count == 0){
            System.out.println("Found no commit with that message.");
        }
    }

    public void globallog() {
        for (Commit current : things.values()){
            System.out.println(current);
        }
        System.out.println("");
    }

    public void status(){
        System.out.println("=== Branches ===");
        String[] keySet = branches.keySet().toArray(new String[branches.keySet().size()]);
        Arrays.sort(keySet);
        for (String eachBranch : keySet){
            if (eachBranch.equals(currentBranch)){
                System.out.print("*");
            }
            System.out.println(eachBranch);
        }
        System.out.println();


        System.out.println("=== Staged Files ===");
        File[] stagedFiles = STAGING_AREA.listFiles();
        ArrayList<String> staging = new ArrayList<>();

        if (stagedFiles != null){
            for (File staged : stagedFiles){
                String name = staged.getName();
                staging.add(name);
            }

            for (String str : removed){
                staging.remove(str);
            }

            Collections.sort(staging);
            for (String str : staging){
                System.out.println(str);
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if(!removed.isEmpty()){
            List<String> removing = new ArrayList<>(removed);
            Collections.sort(removing);
            for (String str : removing){
                System.out.println(str);
            }
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
    }

    public void branch(String branchName){
        for (String str : branches.keySet()){
            if (str.equals(branchName)){
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }

        branches.put(branchName,head);
    }

    public void rmbranch(String branchName){
        if (branchName.equals(currentBranch)){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        String removedBranch = branches.remove(branchName);
        if (removedBranch == null){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    public void reset(String commitID){
        if (!things.containsKey(commitID)){
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        Commit current = things.get(head);
        for (String filename : filesInCWD){
            if (!current.getContents().containsKey(filename) && filesInCWD.contains(filename)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (String filename : filesInCWD){
            checkout(filename);
        }
        for (File stagedFile : STAGING_AREA.listFiles()){
            stagedFile.delete();
        }
    }



}
