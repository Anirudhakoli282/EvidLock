export class Settings {
    static get API_ENDPOINT(): string {
        if (typeof window !== 'undefined' && window.location && window.location.hostname) {
            return `http://${window.location.hostname}:8081/`;
        }
        return 'http://localhost:8081/';
    }
}