package com.example.ucityventure;

public class User {
    private String email;
    private String id;
    private String nome;
    private int numRatings;
    private Float somaRatings;

    public User(String email, String id, String nome, int numRatings, Float somaRatings) {
        this.email = email;
        this.id = id;
        this.nome = nome;
        this.numRatings = numRatings;
        this.somaRatings = somaRatings;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public Float getSomaRatings() {
        return somaRatings;
    }

    public void setSomaRatings(Float somaRatings) {
        this.somaRatings = somaRatings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("email='").append(email).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", nome='").append(nome).append('\'');
        sb.append(", numRatings=").append(numRatings);
        sb.append(", somaRatings=").append(somaRatings);
        sb.append('}');
        return sb.toString();
    }
}
