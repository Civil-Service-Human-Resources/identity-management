package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CivilServantDto {
    private String fullName;
    private String lineManagerEmailAddress;
    private Grade grade;
    private OrganisationalUnit organisationalUnit;
    private Profession profession;
    private List<Profession> otherAreasOfWork;
    private List<Interest> interests;

    public String getDisplayOtherAreasOfWork() {
        return otherAreasOfWork.stream().map(Profession::getName).collect(Collectors.joining(", "));
    }

    public String getDisplayInterests() {
        return interests.stream().map(Interest::getName).collect(Collectors.joining(", "));
    }
}
