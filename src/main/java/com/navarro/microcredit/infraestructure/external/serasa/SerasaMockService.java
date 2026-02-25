package com.navarro.microcredit.infraestructure.external.serasa;

import org.springframework.stereotype.Service;

@Service
public class SerasaMockService {
    /**
     * Simula a consulta ao Serasa.
     * Retorna TRUE se o cliente está com o nome limpo (elegível).
     * Retorna FALSE se o cliente está com o nome sujo.
     */
    public boolean isEligible(String cpf) {
        if (cpf.endsWith("99")) {
            return false;
        }
        return true;
    }
}
