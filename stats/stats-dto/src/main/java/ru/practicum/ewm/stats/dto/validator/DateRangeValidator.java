package ru.practicum.ewm.stats.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.ewm.stats.dto.StatsRequestDto;

public class DateRangeValidator implements ConstraintValidator<DateRangeValid, StatsRequestDto> {

    @Override
    public boolean isValid(StatsRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            return true; // Null проверяется @NotNull
        }
        if (dto.getEnd().isBefore(dto.getStart())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                    .addPropertyNode("end")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
