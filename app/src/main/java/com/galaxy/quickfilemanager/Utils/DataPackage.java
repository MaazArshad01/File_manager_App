package com.galaxy.quickfilemanager.Utils;

/**
 * Created by Umiya Mataji on 1/10/2017.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arpit on 01-08-2015.
 */
public class DataPackage implements Parcelable {
    public static final Creator<DataPackage> CREATOR = new Creator<DataPackage>() {
        @Override
        public DataPackage createFromParcel(Parcel in) {
            return new DataPackage(in);
        }

        @Override
        public DataPackage[] newArray(int size) {
            return new DataPackage[size];
        }
    };
    int id, p1, p2;
    long total, done;
    boolean completed = false, move = false;
    String name;
    String ServiceType;
    String target_filepath;

    protected DataPackage(Parcel in) {
        id = in.readInt();
        p1 = in.readInt();
        p2 = in.readInt();
        total = in.readLong();
        done = in.readLong();
        completed = in.readByte() != 0;
        move = in.readByte() != 0;
        name = in.readString();
        ServiceType = in.readString();
        target_filepath = in.readString();
    }

    public DataPackage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getDone() {
        return done;
    }

    public void setDone(long done) {
        this.done = done;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(p1);
        dest.writeInt(p2);
        dest.writeLong(total);
        dest.writeLong(done);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeByte((byte) (move ? 1 : 0));
        dest.writeString(name);
        dest.writeString(ServiceType);
        dest.writeString(target_filepath);
    }

    public String getServiceType() {
        return ServiceType;
    }

    public void setServiceType(String serviceType) {
        ServiceType = serviceType;
    }

    public String getTarget_filepath() {
        return target_filepath;
    }

    public void setTarget_filepath(String target_filepath) {
        this.target_filepath = target_filepath;
    }
}
