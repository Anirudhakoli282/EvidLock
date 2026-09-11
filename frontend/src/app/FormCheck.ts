import { NgbDate } from '@ng-bootstrap/ng-bootstrap';

export class FormCheck {
    static emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/;

    static checkNome(nome: string): boolean {
        const regex = /^[a-zA-Z ]+$/;
        return regex.test(nome);
    }

    static checkPassword(password: string): boolean {
        const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.])[A-Za-z\d@$!%*?&.]{8,}$/;
        return regex.test(password);
    }


    static checkTelefono(telefono: string): boolean {
        // Verifica se il numero contiene almeno 9 cifre (fisso) o 10 cifre (cellulare)
        return /^\d{9,10}$/.test(telefono);
    }

    static checkEmail(email: string): boolean {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    static checkCodiceFiscale(cf: string): boolean {
        const regex = /[a-zA-Z]{6}\d{2}[a-zA-Z]\d{2}[a-zA-Z]\d{3}[a-zA-Z]/;
        return regex.test(cf);
    }

    static dataNascitaMinima(dataNascita: NgbDate): boolean {
        const today = new Date();
        const minAgeDate = new NgbDate(today.getFullYear() - 18, today.getMonth() + 1, today.getDate());

        if (dataNascita.year > minAgeDate.year ||
            (dataNascita.year === minAgeDate.year && dataNascita.month > minAgeDate.month) ||
            (dataNascita.year === minAgeDate.year && dataNascita.month === minAgeDate.month && dataNascita.day > minAgeDate.day)) {
            return false;
        }
        return true;
    }

    static compareTwoDates(date1: NgbDate, date2: NgbDate): boolean {
        if (date1.year < date2.year ||
            (date1.year === date2.year && date1.month < date2.month) ||
            (date1.year === date2.year && date1.month === date2.month && date1.day < date2.day)) {
            return false;
        }
        return true;
    }

    static compareTwoPasswords(password1: string, password2: string): boolean {
        if (password1 === password2) {
            return true;
        }
        return false;
    }

    static compareFutureDate(date: NgbDate): boolean {
        const today = new Date();
        const todayDate = new NgbDate(today.getFullYear(), today.getMonth() + 1, today.getDate());

        if (date.year > todayDate.year ||
            (date.year === todayDate.year && date.month > todayDate.month) ||
            (date.year === todayDate.year && date.month === todayDate.month && date.day > todayDate.day)) {
            return false;
        }
        return true;
    }

    static NgbDateToDateString(ngbDate: NgbDate): string {
        // Converte un oggetto NgbDate in una stringa nel formato 'yyyy-MM-dd'
        // Aggiunge uno zero davanti al mese e al giorno se sono composti da un solo carattere
        let month = ngbDate.month.toString();
        let day = ngbDate.day.toString();
        if (ngbDate.month < 10) {
            month = '0' + month;
        }
        if (ngbDate.day < 10) {
            day = '0' + day;
        }
        return ngbDate.year + '-' + month + '-' + day;
    }

    static checkPIva(pIva: string): boolean {
        const regex = /^\d{11}$/;
        return regex.test(pIva);
    }
}