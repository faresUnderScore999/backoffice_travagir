package frontoffice.utils;

public class ViewContext {
    private static final ViewContext instance = new ViewContext();

    private Integer reclamationReservationId;

    private ViewContext() {
    }

    public static ViewContext getInstance() {
        return instance;
    }

    public Integer getReclamationReservationId() {
        return reclamationReservationId;
    }

    public void setReclamationReservationId(Integer reclamationReservationId) {
        this.reclamationReservationId = reclamationReservationId;
    }

    public void clear() {
        reclamationReservationId = null;
    }
}
