package cn.lessask.word.model;

/**
 * Created by huangji on 2016/10/12.
 */
public class Goods {
    private int id;
    private String name;
    private int amount;
    private float money;

    public Goods(int id, String name, int amount, float money) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.money = money;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }
}
