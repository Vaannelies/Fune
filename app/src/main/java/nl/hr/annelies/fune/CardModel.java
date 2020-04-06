package nl.hr.annelies.fune;

public class CardModel {
    private int id;
    private int image;
    private String title;
    private String desc;

    public CardModel(int image, String title, String desc, int id) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
