package com.noduplex.model;

/**
 * Represents the properties of any system file
 *
 * @author natanaelsimoes
 */
public class TMDuplexFile {

    private String name;
    private String path;
    private Long size;
    private String hash;
    private String extension;   

    /**
     * @return the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the file name and loads the extension
     *
     * @param name the file name
     */
    public void setName(String name) {
        this.name = name;
        this.extension = name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * @return the absolute file path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the absolute file path
     *
     * @param path the absolute file path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the file size
     */
    public Long getSize() {
        return this.size;
    }
    
    public String getSizeStr() {
        return String.valueOf(this.size);
    }

    public String getSizeForHuman() {
        int unit = 1024;
        if (this.size < unit) {
            return this.size + " B";
        }
        int exp = (int) (Math.log(this.size) / Math.log(unit));
        String pre = String.valueOf(("KMGTPE").charAt(exp - 1));
        return String.format("%.1f %sB", this.size / Math.pow(unit, exp), pre);
    }    

    /**
     * Sets the file size (in bytes)
     *
     * @param size the file size
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return the file hash (md5)
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the file hash (so we can compare with others)
     *
     * @param hash the file hash (md5)
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the file extension
     */
    public String getExtension() {
        return extension;
    }

}
