package org.sunbit.addressbook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.sunbit.addressbook.validator.CreateGroup;
import org.sunbit.addressbook.validator.UpdateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@Builder
@AllArgsConstructor
public class Contact {

  public Contact() {}

  @NotNull(groups = UpdateGroup.class, message = "id should not be empty")
  @Null(groups = CreateGroup.class, message = "id should be empty")
  private Long id;

  @NotBlank(
      groups = {UpdateGroup.class, CreateGroup.class},
      message = "name must not be blank")
  private String name;

  @NotBlank(message = "phoneNumber must not be blank")
  private String phoneNumber;
}
