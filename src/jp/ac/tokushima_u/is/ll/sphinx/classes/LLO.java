
package jp.ac.tokushima_u.is.ll.sphinx.classes;


public class LLO {

    private String name;
    private String image;

    public LLO(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
