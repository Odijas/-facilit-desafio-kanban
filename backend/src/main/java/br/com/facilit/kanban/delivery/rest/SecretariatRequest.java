package br.com.facilit.kanban.delivery.rest;

import static br.com.facilit.kanban.delivery.common.InputLimits.NAME_MAX_LENGTH;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecretariatRequest(@NotBlank @Size(max = NAME_MAX_LENGTH) String name) {
}
