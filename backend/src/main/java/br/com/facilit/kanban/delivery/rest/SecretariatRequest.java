package br.com.facilit.kanban.delivery.rest;

import jakarta.validation.constraints.NotBlank;

public record SecretariatRequest(@NotBlank String name) {
}
