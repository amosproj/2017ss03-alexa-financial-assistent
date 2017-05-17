package model.banking.account;

public class StandingOrderResponse {

    private _embedded _embedded;

    private _links _links;

    public model.banking.account._embedded get_embedded() {
        return _embedded;
    }

    public void set_embedded(model.banking.account._embedded _embedded) {
        this._embedded = _embedded;
    }

    public model.banking.account._links get_links() {
        return _links;
    }

    public void set_links(model.banking.account._links _links) {
        this._links = _links;
    }
}
