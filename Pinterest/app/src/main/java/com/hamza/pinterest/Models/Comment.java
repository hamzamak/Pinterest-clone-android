package com.hamza.pinterest.Models;

import com.hamza.pinterest.Utils.UserLoginModel;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    String _id ,content ;
    UserLoginModel owner ;

    Date createdAt ;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserLoginModel getOwner() {
        return owner;
    }

    public void setOwner(UserLoginModel owner) {
        this.owner = owner;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
