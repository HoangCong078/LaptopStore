
package com.laptopstore.model;

import java.math.BigDecimal;

public class Laptop {

    // ===== 1. THUOC TINH =====

    private int laptopId;
    private String laptopName;
    private String brand;
    private Integer categoryId;
    private BigDecimal price;
    private int quantity;
    private String cpu;
    private String ram;
    private String storage;
    private String screen;
    private String tags;

    // ===== 2. CONSTRUCTOR =====

    // Dung khi tao laptop moi (chua co ID)
    public Laptop(String laptopName, String brand,
                  Integer categoryId, BigDecimal price,
                  int quantity, String cpu, String ram,
                  String storage, String screen,
                  String tags) {

        setLaptopName(laptopName);
        setBrand(brand);
        setCategoryId(categoryId);
        setPrice(price);
        setQuantity(quantity);

        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.screen = screen;
        this.tags = tags;
    }

    // Dung khi doc laptop tu database
    public Laptop(int laptopId, String laptopName,
                  String brand, Integer categoryId,
                  BigDecimal price, int quantity,
                  String cpu, String ram,
                  String storage, String screen,
                  String tags) {

        this(laptopName, brand, categoryId,
                price, quantity, cpu, ram,
                storage, screen, tags);

        setLaptopId(laptopId);
    }

    // ===== 3. GETTER / SETTER =====

    public int getLaptopId() {
        return laptopId;
    }

    public void setLaptopId(int laptopId) {
        if (laptopId < 0) {
            throw new IllegalArgumentException(
                    "Ma laptop khong hop le"
            );
        }
        this.laptopId = laptopId;
    }

    public String getLaptopName() {
        return laptopName;
    }

    public void setLaptopName(String laptopName) {
        if (laptopName == null || laptopName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ten laptop khong duoc de trong"
            );
        }
        this.laptopName = laptopName.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException(
                    "Hang laptop khong duoc de trong"
            );
        }
        this.brand = brand.trim();
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Ma danh muc phai lon hon 0"
            );
        }
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null ||
                price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Gia laptop khong duoc am"
            );
        }
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "So luong khong duoc am"
            );
        }
        this.quantity = quantity;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // ===== 4. HIEN THI THONG TIN =====

    @Override
    public String toString() {
        return "Laptop{" +
                "id=" + laptopId +
                ", name='" + laptopName + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", tags='" + tags + '\'' +
                '}';
    }
}
