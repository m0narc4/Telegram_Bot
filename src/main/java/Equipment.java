public class Equipment {
    public int equipmentId;
    public String equipmentName;
    public double price;
    public double firstPrice;
    public boolean isBooked;

    public Equipment(int anInt, String name, double price, double firstPrice) {
        this.equipmentId = anInt;
        this.equipmentName = name;
        this.price = price;
        this.firstPrice = firstPrice;
        isBooked = false;
    }

    public Equipment() {

    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public double getPrice() {
        return price;
    }

    public double getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(double firstPrice) {
        this.firstPrice = firstPrice;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
}
