package com.cqupt.garage.pojo;

import lombok.Data;

import java.util.List;

@Data
public class MenuItem {
    private String menusIndex;
    private String title;
    private String icon;
    private List<MenuChild> children;
}
