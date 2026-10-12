
package com.laptopstore.model;

public class Category {

    // 1. Thuoc tinh
    private int categoryId;
    private String categoryName;
    private String description;

    // 2. Constructor khi them danh muc moi
    public Category(String categoryName, String description) {
        setCategoryName(categoryName);
        setDescription(description);
    }

    // 3. Constructor khi doc du lieu tu MySQL
    public Category(int categoryId,
                    String categoryName,
                    String description) {

        this(categoryName, description);
        setCategoryId(categoryId);
    }

    // 4. Getter va Setter

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        if (categoryId < 0) {
            throw new IllegalArgumentException(
                    "Ma danh muc khong hop le"
            );
        }
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        if (categoryName == null ||
                categoryName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ten danh muc khong duoc de trong"
            );
        }

        if (categoryName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Ten danh muc qua 100 ky tu"
            );
        }

        this.categoryName = categoryName.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 5. Hien thi thong tin
    @Override
    public String toString() {
        return categoryName;
    }
}
