package com.hugoserve.metalbroker.service;



public interface DbMetalAdminService {

    String create(String json);

    String update(String code, String json);

    String deactivate(String code);
}
