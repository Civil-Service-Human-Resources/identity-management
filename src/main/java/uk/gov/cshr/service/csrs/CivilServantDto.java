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

    public String getDisplayLineManagerEmail() {
        return this.lineManagerEmailAddress == null ? "None" : this.lineManagerEmailAddress;
    }

    public String getDisplayOrganisation() {
        return this.organisationalUnit == null ? "None" : this.organisationalUnit.toDisplayString();
    }

    public String getDisplayGrade() {
        return this.grade == null ? "None" : this.grade.getName();
    }

    public String getDisplayProfession() {
        return this.profession == null ? "None" : this.profession.getName();
    }

    public String getDisplayOtherAreasOfWork() {
        if (otherAreasOfWork.size() > 0) {
            return otherAreasOfWork.stream().map(Profession::getName).collect(Collectors.joining(", "));
        } else {
            return "None";
        }
    }

    public String getDisplayInterests() {
        if (interests.size() > 0) {
            return interests.stream().map(Interest::getName).collect(Collectors.joining(", "));
        } {
            return "None";
        }
    }
}
