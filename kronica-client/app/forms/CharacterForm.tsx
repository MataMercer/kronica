"use client";
import {Controller, SubmitHandler, useForm} from "react-hook-form";
import {refreshArticles} from "../actions";
import MDEditor from "@uiw/react-md-editor";
import React, {useEffect} from "react";
import UploadInput, {FileInput} from "../components/inputs/UploadInput";
import useCurrentUser from "../hooks/useCurrentUser";
import {useToast} from "@/components/hooks/use-toast";
import {CircleMinus, CirclePlus} from "lucide-react";
import {useCharacter} from "@/app/hooks/useCharacters";
import {useRouter} from "next/navigation";

type Trait = {
    name: string;
    value: string;
};

type Inputs = {
    name: string;
    traits: Trait[];
    body: string;
    uploadedAttachments: FileInput[];
    uploadedProfilePictures: FileInput[];
};

export default function CharacterForm({id}: { id?: number }) {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: {errors},
        control,
    } = useForm<Inputs>({
        defaultValues: {
            name: "ExampleName",
            body: "ExampleBody",
            uploadedAttachments: [],
            uploadedProfilePictures: [],
            traits: [],
        },
    });

    const {
        user,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();

    const {toast} = useToast();
    const {character, mutate: mutateCharacter} = useCharacter(id)
    const router = useRouter();

    const onSubmit: SubmitHandler<Inputs> = async (data) => {

        if (id) {
            await handleUpdateCharacter(id, data);
        } else {
            await handleCreateCharacter(data);
        }
    };

    async function handleCreateCharacter(data: Inputs) {
        const formData = new FormData();
        formData.append("name", data.name);
        formData.append("body", data.body);

        let traitsData =
            data.traits.length > 0
                ? data.traits[0].name + ":" + data.traits[0].value
                : "";
        data.traits.forEach((it, index) => {
            if (index != 0) {
                traitsData = traitsData + "," + it.name + ":" + it.value;
            }
        });
        formData.append("traits", traitsData);

        data.uploadedAttachments
            .filter((it) => it.data)
            .map((it) => {
                it.data && formData.append("uploadedAttachments", it.data);
                formData.append(
                    "uploadedAttachmentsMetadata",
                    JSON.stringify(it.metadata)
                );
            });

        data.uploadedProfilePictures
            .filter((it) => it.data)
            .map((it) => {
                it.data && formData.append("uploadedProfilePictures", it.data);
                formData.append(
                    "uploadedProfilePicturesMetadata",
                    JSON.stringify(it.metadata)
                );
            });

        const response = await fetch("http://localhost:7070/api/characters", {
            method: "POST",
            credentials: "include",
            body: formData,
        });

        if (response.ok) {
            toast({
                title: "Character Successfully Created",
                description: data.name,
            });
            reset();
        }
    }

    async function handleUpdateCharacter(id: number, data: Inputs) {
        const formData = new FormData();
        formData.append("id", id.toString());
        formData.append("name", data.name);
        formData.append("body", data.body);

        let traitsData =
            data.traits.length > 0
                ? data.traits[0].name + ":" + data.traits[0].value
                : "";
        data.traits.forEach((it, index) => {
            if (index != 0) {
                traitsData = traitsData + "," + it.name + ":" + it.value;
            }
        });
        formData.append("traits", traitsData);

        data.uploadedAttachments
            .map((it) => {
                if (!it.metadata.id && it.data) {
                    formData.append("uploadedAttachments", it.data);
                }
                formData.append(
                    "uploadedAttachmentsMetadata",
                    JSON.stringify(it.metadata)
                );
            });

        data.uploadedProfilePictures.map((it) => {
            if (!it.metadata.id && it.data) {
                formData.append("uploadedProfilePictures", it.data);
            }
            formData.append(
                "uploadedProfilePicturesMetadata",
                JSON.stringify(it.metadata)
            )
        })

        const response = await fetch(
            `http://localhost:7070/api/characters/${id}`,
            {
                method: "PUT",
                credentials: "include",
                body: formData,
            }
        );

        if (response.ok) {
            router.push(`/characters/${id}`);
            toast({
                title: "Character Successfully Updated",
                description: data.name,
            });
        } else {
            const errorData = await response.json();
            toast({
                title: "Error Updating Character",
                description: errorData.message || "An error occurred",
                variant: "destructive"
            });
            console.error(errorData as string);
        }
    }

    useEffect(() => {
        if (id && character) {
            const {name, body, traits, attachments, profilePictures} = character;
            setValue("name", name);
            setValue("body", body);
            console.log(traits)
            if (traits && traits.length > 0) {
                setValue("traits", traits.map(it => ({name: it.name, value: it.value})));
            }
            if (attachments && attachments.length > 0) {
                setValue("uploadedAttachments", attachments.map(
                    (a, index) =>
                        ({
                            id: index.toString(),
                            url: `http://localhost:7070/api/files/serve/${a.storageId}/${a.name}`,
                            metadata: {
                                id: a.id,
                                caption: a.caption,
                                delete: false
                            }
                        })));
            }

            if (profilePictures && profilePictures.length > 0) {
                setValue("uploadedProfilePictures", profilePictures.map(
                    (a, index) =>
                        ({
                            id: index.toString(),
                            url: `http://localhost:7070/api/files/serve/${a.storageId}/${a.name}`,
                            metadata: {
                                id: a.id,
                                caption: a.caption,
                                delete: false
                            }
                        })));
            }
        }
    }, [character, id, setValue]);

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-4 py-4">
                <label className="flex flex-col" htmlFor="name">
                    Name
                    <input
                        {...register("name", {required: true})}
                        id="name"
                        defaultValue="Unnamed"
                    />
                </label>

                <label className="flex flex-col" htmlFor="">
                    Profile Pictures
                    <Controller
                        name="uploadedProfilePictures"
                        control={control}
                        render={({field}) => (
                            <UploadInput
                                id="uploadedProfilePictures"
                                setFileInputs={(files: FileInput[]) => {
                                    setValue("uploadedProfilePictures", files);
                                }}
                                fileInputs={field.value}
                            />
                        )}
                    ></Controller>
                </label>
                <label className="flex flex-col">
                    Traits
                    <Controller
                        name="traits"
                        control={control}
                        render={({field}) => (
                            <div>
                                {field.value.map((it, index) => (
                                    <div
                                        className="flex flex-row space-y-2 space-x-2"
                                        key={index}
                                    >
                                        <span className="content-center">
                                            Name{" "}
                                        </span>{" "}
                                        <input
                                            value={field.value[index].name}
                                            onChange={(e) => {
                                                const newFieldVal = field.value;
                                                field.value[index].name =
                                                    e.target.value;
                                                setValue("traits", newFieldVal);
                                            }}
                                        />
                                        <span className="content-center">
                                            Value
                                        </span>{" "}
                                        <input
                                            value={field.value[index].value}
                                            onChange={(e) => {
                                                const newFieldVal = field.value;
                                                field.value[index].value =
                                                    e.target.value;
                                                setValue("traits", newFieldVal);
                                            }}
                                        />
                                        <button
                                            type="button"
                                            className="flex"
                                            onClick={() => {
                                                setValue(
                                                    "traits",
                                                    field.value.toSpliced(
                                                        index,
                                                        1
                                                    )
                                                );
                                            }}
                                        >
                                            <CircleMinus/>
                                            <div>Remove</div>
                                        </button>
                                    </div>
                                ))}

                                <button
                                    type="button"
                                    className="flex"
                                    onClick={() => {
                                        setValue("traits", [
                                            ...field.value,
                                            {name: "", value: ""},
                                        ]);
                                    }}
                                >
                                    <CirclePlus/>
                                    <div>Additional Trait</div>
                                </button>
                            </div>
                        )}
                    />
                </label>

                <label className="flex flex-col" htmlFor="body">
                    Body
                    <Controller
                        name="body"
                        control={control}
                        defaultValue="Empty body"
                        render={({field}) => (
                            <div data-color-mode="light">
                                <MDEditor
                                    value={field.value}
                                    onChange={field.onChange}
                                />
                            </div>
                        )}
                    />
                </label>

                <label className="flex flex-col" htmlFor="">
                    File Attachments (Gallery)
                    <Controller
                        name="uploadedAttachments"
                        control={control}
                        render={({field}) => (
                            <UploadInput
                                id="uploadedAttachments"
                                setFileInputs={(files: FileInput[]) => {
                                    setValue("uploadedAttachments", files);
                                }}
                                fileInputs={field.value}
                            />
                        )}
                    ></Controller>
                </label>
            </div>
            <button className="button" type="submit">
                Submit
            </button>
        </form>
    );
}
