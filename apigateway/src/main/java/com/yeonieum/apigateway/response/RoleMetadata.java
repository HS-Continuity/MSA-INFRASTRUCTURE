package com.yeonieum.apigateway.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@NoArgsConstructor
public class RoleMetadata {
    List<String> roles;
    String Methods;
    public List<String> getRoles() {
        return roles;
    }
    public String getMethods() {
        return Methods;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public void setMethods(String Methods) {
        this.Methods = Methods;
    }

}
