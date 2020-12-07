package com.example.yikuaiju.service;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IOperatorService {

    Page<Map<String, Object>> userList(int pageNum, int pageSize);

    List<Map<String, Object>> userList();

    Page<Map<String, Object>> gameList(int pageNum, int pageSize);

    List<Map<String, Object>> gameList();
}
