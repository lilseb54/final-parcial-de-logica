package src.modelo;

public class Predio {
    private String npn;
    private String municipio;
    private String direccion;
    private String ficha;

    public Predio(String npn, String municipio, String direccion, String ficha) {
        this.npn = npn != null ? npn.trim() : "";
        this.municipio = municipio != null ? municipio.trim() : "";
        this.direccion = direccion != null ? direccion.trim() : "";
        this.ficha = ficha != null ? ficha.trim() : "";
    }

    public String getNpn() { return npn; }
    public String getMunicipio() { return municipio; }
    public String getDireccion() { return direccion; }
    public String getFicha() { return ficha; }

    public String getValorPorColumna(int columna) {
        switch (columna) {
            case 1: return this.npn;
            case 2: return this.municipio;
            case 3: return this.direccion;
            case 4: return this.ficha;
            default: return "";
        }
    }
}