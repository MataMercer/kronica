"use client";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import {Controller, SubmitHandler, useForm} from "react-hook-form";
import {useEffect, useState} from "react";
import React from "react";
import KronologizeInput, {
    OrderInput,
} from "../components/inputs/KronologizeInput";
import useCurrentUser from "../hooks/useCurrentUser";
import {refreshArticles} from "../actions";
import {fetchAllArticles} from "../fetch/articles";
import {useArticles} from "../hooks/useArticles";
import {useRouter} from "next/navigation";
import EmptyPlaceholder from "@/components/CustomUi/EmptyPlaceholder";

type Inputs = {
    order: OrderInput[];
};

export default function ReorderTimelineForm({
                                                timelineId,
                                            }: {timelineId?: number}) {
    const {user, loading, loggedOut, mutate} = useCurrentUser();
    const userId = user && user.id;
    const {articles, mutate: mutateArticles} = useArticles(
        userId?.toString(),
        timelineId ? timelineId.toString() : undefined
    );

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
            order:
                articles?.map((it) => ({
                    id: it.id.toString(),
                    title: it.title,
                })) || [],
        },
    });

    const router = useRouter();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const formattedData = {
            order: data.order.map((it) => parseInt(it.id)),
        };
        const response = await fetch(
            `http://localhost:7070/api/timelines/${timelineId}/order`,
            {
                method: "PUT",
                credentials: "include",
                body: JSON.stringify(formattedData),
            }
        );
        if(response.ok){
            reset()
            router.push(`/users/${userId}/${timelineId}`)
        }
    };

    return (

        <form onSubmit={handleSubmit(onSubmit)}>
            Reorder the presentation of articles for a timeline
            so that they are shown chronologically.

            <div className="flex flex-col gap-4 py-4">
                <label className="flex flex-col" htmlFor="">
                    Article Order
                    {articles && articles.length > 0 ?
                        <Controller
                            name="order"
                            control={control}
                            render={({field}) => (
                                <KronologizeInput
                                    id="order"
                                    setOrder={(
                                        orderInput: OrderInput[]
                                    ) => {
                                        setValue("order", orderInput);
                                    }}
                                    order={field.value}
                                />
                            )}
                        ></Controller>
                        : <EmptyPlaceholder/>
                    }
                </label>
            </div>
            <button className="button" type="submit" disabled={!articles || articles.length == 0}>
                Save Changes
            </button>
        </form>
    );
}
