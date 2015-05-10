package jp.ac.tokushima_u.is.ll.sphinx.classes;


public class LogItem{
    private Long id;
    private String itemId;
    private String photoUrl;
    private String title;
    private Integer syncType;
    private String attached;
    
    public LogItem(){
        
    }
    
    public LogItem(Long id, String itemId, String photoUrl, String title, Integer syncType,
            String attached) {
        this.id = id;
        this.itemId = itemId;
        this.photoUrl = photoUrl;
        this.title = title;
        this.syncType = syncType;
        this.attached = attached;
    }
    
// ↓馬鹿すぎる設計だなー（しろめ）
//    public LogItem(Cursor cursor){
//        if(cursor!=null){
//            this.id = cursor.getLong(ItemsQuery._ID);
//            this.itemId = cursor.getString(ItemsQuery.ITEM_ID);
//            this.photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
//            this.title = cursor.getString(ItemsQuery.TITLES);
//            this.syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);
//            this.attached = cursor.getString(ItemsQuery.ATTACHED);
//        }
//    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSyncType() {
        return syncType;
    }

    public void setSyncType(Integer syncType) {
        this.syncType = syncType;
    }

    public String getAttached() {
        return attached;
    }

    public void setAttached(String attached) {
        this.attached = attached;
    }
}