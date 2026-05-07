package com.example.demo.modules.request.dto;

public class SampleDTO {
    private String barcode;
    private String status;
    private Long recipeId;
    private String recipeName; // Optional, for display in details
    private String recipeParameters;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeParameters() {
        return recipeParameters;
    }

    public void setRecipeParameters(String recipeParameters) {
        this.recipeParameters = recipeParameters;
    }
}
