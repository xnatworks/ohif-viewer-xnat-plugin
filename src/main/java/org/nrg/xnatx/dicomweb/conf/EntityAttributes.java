/********************************************************************
 * Copyright (c) 2023, Institute of Cancer Research
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * (3) Neither the name of the Institute of Cancer Research nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/
package org.nrg.xnatx.dicomweb.conf;

import org.dcm4che3.data.Tag;

public class EntityAttributes
{
	static public final int[] PATIENT_ATTRS = {
		Tag.SpecificCharacterSet,
		Tag.PatientName,
		Tag.PatientID,
		Tag.IssuerOfPatientID,
		Tag.TypeOfPatientID,
		Tag.IssuerOfPatientIDQualifiersSequence,
		Tag.PatientBirthDate,
		Tag.PatientBirthTime,
		Tag.PatientBirthDateInAlternativeCalendar,
		Tag.PatientDeathDateInAlternativeCalendar,
		Tag.PatientAlternativeCalendar,
		Tag.PatientSex,
		Tag.PatientInsurancePlanCodeSequence,
		Tag.PatientPrimaryLanguageCodeSequence,
		Tag.QualityControlSubject,
		Tag.StrainDescription,
		Tag.StrainNomenclature,
		Tag.StrainStockSequence,
		Tag.StrainAdditionalInformation,
		Tag.StrainCodeSequence,
		Tag.GeneticModificationsCodeSequence,
		Tag.OtherPatientNames,
		Tag.OtherPatientIDsSequence,
		Tag.PatientBirthName,
		Tag.PatientAddress,
		Tag.PatientMotherBirthName,
		Tag.MilitaryRank,
		Tag.BranchOfService,
		Tag.MedicalRecordLocator,
		Tag.ReferencedPatientPhotoSequence,
		Tag.CountryOfResidence,
		Tag.RegionOfResidence,
		Tag.PatientTelephoneNumbers,
		Tag.PatientTelecomInformation,
		Tag.EthnicGroup,
		Tag.PatientReligiousPreference,
		Tag.PatientSpeciesDescription,
		Tag.PatientSpeciesCodeSequence,
		Tag.PatientBreedDescription,
		Tag.PatientBreedCodeSequence,
		Tag.BreedRegistrationSequence,
		Tag.ResponsiblePerson,
		Tag.ResponsiblePersonRole,
		Tag.ResponsibleOrganization,
		Tag.PatientComments,
		Tag.ClinicalTrialSponsorName,
		Tag.ClinicalTrialProtocolID,
		Tag.ClinicalTrialProtocolName,
		Tag.ClinicalTrialSiteID,
		Tag.ClinicalTrialSiteName,
		Tag.ClinicalTrialSubjectID,
		Tag.ClinicalTrialSubjectReadingID,
		Tag.PatientIdentityRemoved,
		Tag.DeidentificationMethod,
		Tag.DeidentificationMethodCodeSequence,
		Tag.ClinicalTrialProtocolEthicsCommitteeName,
		Tag.ClinicalTrialProtocolEthicsCommitteeApprovalNumber,
		Tag.SpecialNeeds,
		Tag.PertinentDocumentsSequence,
		Tag.PatientClinicalTrialParticipationSequence,
		Tag.ConfidentialityConstraintOnPatientDataDescription
	};

	static public final int[] STUDY_ATTRS = {
		Tag.SpecificCharacterSet,
		Tag.StudyDate,
		Tag.StudyTime,
		Tag.AccessionNumber,
		Tag.IssuerOfAccessionNumberSequence,
		Tag.ReferringPhysicianName,
		Tag.TimezoneOffsetFromUTC,
		Tag.StudyDescription,
		Tag.ProcedureCodeSequence,
		Tag.PhysiciansOfRecord,
		Tag.PatientAge,
		Tag.PatientSize,
		Tag.PatientSizeCodeSequence,
		Tag.PatientWeight,
		Tag.PatientBodyMassIndex,
		Tag.MeasuredAPDimension,
		Tag.MeasuredLateralDimension,
		Tag.MedicalAlerts,
		Tag.Allergies,
		Tag.SmokingStatus,
		Tag.PregnancyStatus,
		Tag.LastMenstrualDate,
		Tag.PatientState,
		Tag.AdmittingDiagnosesDescription,
		Tag.AdmittingDiagnosesCodeSequence,
		Tag.AdmissionID,
		Tag.IssuerOfAdmissionIDSequence,
		Tag.RouteOfAdmissions,
		Tag.ReasonForVisit,
		Tag.ReasonForVisitCodeSequence,
		Tag.Occupation,
		Tag.AdditionalPatientHistory,
		Tag.ServiceEpisodeID,
		Tag.ServiceEpisodeDescription,
		Tag.IssuerOfServiceEpisodeIDSequence,
		Tag.PatientSexNeutered,
		Tag.StudyInstanceUID,
		Tag.StudyID
	};

	static public final int[] SERIES_ATTRS = {
		Tag.SpecificCharacterSet,
		Tag.SeriesDate,
		Tag.SeriesTime,
		Tag.Modality,
		Tag.Manufacturer,
		Tag.InstitutionName,
		Tag.InstitutionCodeSequence,
		Tag.TimezoneOffsetFromUTC,
		Tag.StationName,
		Tag.SeriesDescription,
		Tag.InstitutionalDepartmentName,
		Tag.InstitutionalDepartmentTypeCodeSequence,
		Tag.InstitutionAddress,
		Tag.PerformingPhysicianName,
		Tag.ManufacturerModelName,
		Tag.ReferencedPerformedProcedureStepSequence,
		Tag.AnatomicRegionSequence,
		Tag.BodyPartExamined,
		Tag.SeriesInstanceUID,
		Tag.SeriesNumber,
		Tag.Laterality,
		Tag.PerformedProcedureStepStartDate,
		Tag.PerformedProcedureStepStartTime,
		Tag.PerformedProcedureStepEndDate,
		Tag.PerformedProcedureStepEndTime,
		Tag.PerformedProtocolCodeSequence,
		Tag.RequestAttributesSequence
	};

	static public final int[] INSTANCE_ATTRS = {
		Tag.SpecificCharacterSet,
		Tag.ImageType,
		Tag.InstanceCreationDate,
		Tag.InstanceCreationTime,
		Tag.SOPClassUID,
		Tag.SOPInstanceUID,
		Tag.ContentDate,
		Tag.ContentTime,
		Tag.TimezoneOffsetFromUTC,
		Tag.ReferencedSeriesSequence,
		Tag.AnatomicRegionSequence,
		Tag.InstanceNumber,
		Tag.NumberOfFrames,
		Tag.Rows,
		Tag.Columns,
		Tag.BitsAllocated,
		Tag.ObservationDateTime,
		Tag.ConceptNameCodeSequence,
		Tag.VerifyingObserverSequence,
		Tag.ReferencedRequestSequence,
		Tag.CompletionFlag,
		Tag.VerificationFlag,
		Tag.ContentTemplateSequence,
		Tag.DocumentTitle,
		Tag.MIMETypeOfEncapsulatedDocument,
		Tag.ContentLabel,
		Tag.ContentDescription,
		Tag.PresentationCreationDate,
		Tag.PresentationCreationTime,
		Tag.ContentCreatorName,
		Tag.IdenticalDocumentsSequence,
		Tag.CurrentRequestedProcedureEvidenceSequence,
		Tag.ConcatenationUID,
		Tag.SOPInstanceUIDOfConcatenationSource,
		Tag.ContainerIdentifier,
		Tag.AlternateContainerIdentifierSequence,
		Tag.IssuerOfTheContainerIdentifierSequence,
		Tag.SpecimenUID,
		Tag.SpecimenIdentifier,
		Tag.IssuerOfTheSpecimenIdentifierSequence
	};
}
