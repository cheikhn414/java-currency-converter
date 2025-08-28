class CurrencyConverter {
    constructor() {
        this.apiBaseUrl = '/currency-converter/api';
        this.currencies = [];
        this.lastConversion = null;
        
        this.initializeElements();
        this.attachEventListeners();
        this.loadCurrencies();
    }
    
    initializeElements() {
        this.elements = {
            form: document.getElementById('converterForm'),
            amount: document.getElementById('amount'),
            fromCurrency: document.getElementById('fromCurrency'),
            toCurrency: document.getElementById('toCurrency'),
            swapButton: document.getElementById('swapButton'),
            convertButton: document.getElementById('convertButton'),
            buttonText: document.getElementById('buttonText'),
            loadingSpinner: document.getElementById('loadingSpinner'),
            result: document.getElementById('result'),
            error: document.getElementById('error'),
            originalAmount: document.getElementById('originalAmount'),
            originalCurrency: document.getElementById('originalCurrency'),
            convertedAmount: document.getElementById('convertedAmount'),
            convertedCurrency: document.getElementById('convertedCurrency'),
            rateDisplay: document.getElementById('rateDisplay'),
            timestamp: document.getElementById('timestamp'),
            errorMessage: document.getElementById('errorMessage')
        };
    }
    
    attachEventListeners() {
        this.elements.form.addEventListener('submit', (e) => this.handleSubmit(e));
        this.elements.swapButton.addEventListener('click', () => this.swapCurrencies());
        
        this.elements.amount.addEventListener('input', () => this.handleAmountInput());
        this.elements.fromCurrency.addEventListener('change', () => this.handleCurrencyChange());
        this.elements.toCurrency.addEventListener('change', () => this.handleCurrencyChange());
        
        this.elements.amount.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.handleSubmit(e);
            }
        });
    }
    
    async loadCurrencies() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/currencies`);
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            
            this.currencies = await response.json();
            this.populateCurrencySelects();
            this.setDefaultCurrencies();
            
        } catch (error) {
            console.error('Erreur lors du chargement des devises:', error);
            this.showError('Impossible de charger la liste des devises. Veuillez recharger la page.');
        }
    }
    
    populateCurrencySelects() {
        const fromSelect = this.elements.fromCurrency;
        const toSelect = this.elements.toCurrency;
        
        fromSelect.innerHTML = '<option value="">Sélectionnez...</option>';
        toSelect.innerHTML = '<option value="">Sélectionnez...</option>';
        
        this.currencies.forEach(currency => {
            const option1 = new Option(`${currency.code} - ${currency.name}`, currency.code);
            const option2 = new Option(`${currency.code} - ${currency.name}`, currency.code);
            
            fromSelect.appendChild(option1);
            toSelect.appendChild(option2);
        });
    }
    
    setDefaultCurrencies() {
        this.elements.fromCurrency.value = 'USD';
        this.elements.toCurrency.value = 'EUR';
    }
    
    async handleSubmit(e) {
        e.preventDefault();
        
        const amount = parseFloat(this.elements.amount.value);
        const fromCurrency = this.elements.fromCurrency.value;
        const toCurrency = this.elements.toCurrency.value;
        
        if (!this.validateInputs(amount, fromCurrency, toCurrency)) {
            return;
        }
        
        await this.performConversion(amount, fromCurrency, toCurrency);
    }
    
    validateInputs(amount, fromCurrency, toCurrency) {
        if (isNaN(amount) || amount <= 0) {
            this.showError('Veuillez entrer un montant valide et positif.');
            return false;
        }
        
        if (!fromCurrency) {
            this.showError('Veuillez sélectionner la devise source.');
            return false;
        }
        
        if (!toCurrency) {
            this.showError('Veuillez sélectionner la devise de destination.');
            return false;
        }
        
        if (fromCurrency === toCurrency) {
            this.showError('Les devises source et destination doivent être différentes.');
            return false;
        }
        
        return true;
    }
    
    async performConversion(amount, fromCurrency, toCurrency) {
        this.setLoadingState(true);
        this.hideMessages();
        
        try {
            const url = `${this.apiBaseUrl}/convert?amount=${amount}&from=${fromCurrency}&to=${toCurrency}`;
            const response = await fetch(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
            }
            
            const result = await response.json();
            this.displayResult(result);
            this.lastConversion = result;
            
        } catch (error) {
            console.error('Erreur lors de la conversion:', error);
            this.showError(error.message || 'Erreur lors de la conversion. Veuillez réessayer.');
        } finally {
            this.setLoadingState(false);
        }
    }
    
    displayResult(result) {
        const fromCurrencyInfo = this.getCurrencyInfo(result.fromCurrency);
        const toCurrencyInfo = this.getCurrencyInfo(result.toCurrency);
        
        this.elements.originalAmount.textContent = this.formatAmount(result.amount);
        this.elements.originalCurrency.textContent = fromCurrencyInfo.code;
        this.elements.convertedAmount.textContent = this.formatAmount(result.convertedAmount);
        this.elements.convertedCurrency.textContent = toCurrencyInfo.code;
        
        const rateText = `1 ${fromCurrencyInfo.code} = ${this.formatAmount(result.exchangeRate)} ${toCurrencyInfo.code}`;
        this.elements.rateDisplay.textContent = rateText;
        
        this.elements.timestamp.textContent = this.formatTimestamp(result.timestamp);
        
        this.showResult();
    }
    
    getCurrencyInfo(code) {
        return this.currencies.find(c => c.code === code) || { code, name: code, symbol: '' };
    }
    
    formatAmount(amount) {
        return new Intl.NumberFormat('fr-FR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 4
        }).format(amount);
    }
    
    formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        return new Intl.DateTimeFormat('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    }
    
    swapCurrencies() {
        const fromValue = this.elements.fromCurrency.value;
        const toValue = this.elements.toCurrency.value;
        
        this.elements.fromCurrency.value = toValue;
        this.elements.toCurrency.value = fromValue;
        
        this.handleCurrencyChange();
    }
    
    handleAmountInput() {
        this.hideMessages();
        if (this.lastConversion && this.shouldAutoConvert()) {
            this.debounceConversion();
        }
    }
    
    handleCurrencyChange() {
        this.hideMessages();
        if (this.lastConversion && this.shouldAutoConvert()) {
            this.debounceConversion();
        }
    }
    
    shouldAutoConvert() {
        const amount = parseFloat(this.elements.amount.value);
        const fromCurrency = this.elements.fromCurrency.value;
        const toCurrency = this.elements.toCurrency.value;
        
        return !isNaN(amount) && amount > 0 && fromCurrency && toCurrency && fromCurrency !== toCurrency;
    }
    
    debounceConversion() {
        clearTimeout(this.conversionTimeout);
        this.conversionTimeout = setTimeout(() => {
            const amount = parseFloat(this.elements.amount.value);
            const fromCurrency = this.elements.fromCurrency.value;
            const toCurrency = this.elements.toCurrency.value;
            
            if (this.validateInputs(amount, fromCurrency, toCurrency)) {
                this.performConversion(amount, fromCurrency, toCurrency);
            }
        }, 500);
    }
    
    setLoadingState(loading) {
        this.elements.convertButton.disabled = loading;
        
        if (loading) {
            this.elements.buttonText.style.display = 'none';
            this.elements.loadingSpinner.style.display = 'block';
        } else {
            this.elements.buttonText.style.display = 'block';
            this.elements.loadingSpinner.style.display = 'none';
        }
    }
    
    showResult() {
        this.elements.result.style.display = 'block';
        this.elements.error.style.display = 'none';
        
        this.elements.result.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'nearest' 
        });
    }
    
    showError(message) {
        this.elements.errorMessage.textContent = message;
        this.elements.error.style.display = 'flex';
        this.elements.result.style.display = 'none';
        
        this.elements.error.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'nearest' 
        });
    }
    
    hideMessages() {
        this.elements.result.style.display = 'none';
        this.elements.error.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new CurrencyConverter();
});